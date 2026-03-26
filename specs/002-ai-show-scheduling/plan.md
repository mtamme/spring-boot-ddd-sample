# Implementation Plan: AI Show Scheduling Bounded Context

**Branch**: `002-ai-show-scheduling` | **Date**: 2026-03-26 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-ai-show-scheduling/spec.md`

**Note**: This plan has been adapted post-implementation to account for constitution v1.6.0 changes (Clock injection rule, POST collection resource requirement).

## Summary

Implement a new `scheduling` bounded context for AI-driven cinema show scheduling using the Claude LLM API. The aggregate root `Show` enforces future-date and hall-overlap invariants. An AI agent in the infrastructure layer exposes command/query handlers as LLM tools. Hall and movie access is mocked. Constitution v1.6.0 introduced two new rules that require adaptation of the existing implementation: (1) Clock injection instead of `Instant.now()`, and (2) POST endpoints must target collection resource URIs.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0.4, Spring Data JPA, Flyway, Anthropic Java SDK 2.18.0
**Storage**: H2 (local profile), Flyway migrations, JPA ORM XML
**Testing**: JUnit 5, Mockito, ArchUnit, Spring Boot Test
**Target Platform**: Linux server (local development)
**Project Type**: Web service (Spring Boot multi-module)
**Performance Goals**: p95 < 200ms for single-aggregate reads and commands (excluding LLM response time)
**Constraints**: `open-in-view=false`, `ddl-auto=none`, bounded result sets
**Scale/Scope**: Single bounded context, 1 aggregate (Show), 3 value objects, 1 domain service, 1 AI agent

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Module and layer impact is explicit (`scheduling`: `infrastructure -> application -> domain`; depends on `seedwork` not `booking`); domain layer contains only Entity/Event/ValueObject types, @Service domain services, ProblemException subclasses, and interfaces.
- [x] Domain changes keep business rules in domain types; Show aggregate raises ShowScheduled event in the constructor; `equals()`/`hashCode()` use ShowId domain identity, not surrogate key; `private Long id` and `protected` no-arg constructor are at the bottom of the aggregate class.
- [x] Required automated tests are identified for every touched layer, including ArchUnit when structure changes.
- [x] OpenAPI specs, Flyway migrations, domain ORM XMLs (`META-INF/domain/`), query projection ORM XMLs (`META-INF/query/`), and generated interfaces are accounted for; ORM resources are explicitly listed in `spring.jpa.mapping-resources`.
- [x] Performance budget is documented: p95 < 200ms for reads/commands excluding LLM latency; bounded pagination (max 100) on list endpoints; no N+1 risks (single aggregate, no collections).
- [x] All precondition checks use `Contract.require()` and all invariant checks use `Contract.check()`; `ShowException` extends `ProblemException` with `NOT_FOUND_PROBLEM`, `OVERLAP_PROBLEM`, `PAST_SCHEDULE_PROBLEM` constants; `notFound()` uses `Problem.notFound()` (HTTP 404), state violations use `Problem.conflict()` (HTTP 409).
- [x] Naming follows layer conventions: noun accessors (`showId()`, `scheduledAt()`) in domain; `scheduleShow(ScheduleShowCommand)` in command handler; `getShow`/`listShows` in query handlers; `ShowDetailView`/`ShowSummaryView` for projections; `to<Type>()` in `@Component` mappers; `<method>With<State>Should<Behavior>()` in tests; `ShowFixture` factory methods in test fixtures.
- [x] RESTful API design follows conventions: OpenAPI YAML written first; GET endpoints use plural kebab-case nouns and snake_case path params; collection responses wrapped (`{ "shows": [...] }`); single responses flat; errors use `application/problem+json`; offset-limit pagination (max 100). **POST endpoint evaluation**: see POST Collection Resource analysis below.
- [x] Cross-aggregate side effects flow exclusively through domain events; `ShowScheduled` domain event is converted to integration event in infrastructure via `@EventListener`; `ShowEvent` abstract base event class implements seedwork `Event`.
- [x] CQRS separation maintained: `ShowCommandHandler`/`ShowCommandHandlerImpl` for writes; `ShowQueryHandler`/`JpaShowQueryHandler` for reads via named native queries; command/query/result/view records in `command/` and `query/` sub-packages; infrastructure mirrors with `persistence/show/` and `web/show/`.
- [x] Test infrastructure: `PersistenceTest` extends seedwork base (separate read/write transactions); `ControllerTest` with `@MockitoBean` isolation; application-layer tests mock repository dependencies.
- [ ] **VIOLATION — Clock access**: `Show.java:26` calls `Instant.now()` directly. Must be replaced with Clock injection per constitution v1.6.0. See adaptation plan below.

### POST Collection Resource Analysis

The constitution v1.6.0 added: *"POST MUST target a collection resource URI (e.g., `POST /shows/{show_id}/bookings`, `POST /shows`), never an individual resource or action endpoint."*

The `POST /scheduling/agent` endpoint is **not a resource creation endpoint** — it is a conversational interaction that sends a message to the AI agent and returns a response. It returns `200 OK`, not `201 Created`. The POST collection resource rule applies specifically to "resource creation (initiates a new aggregate)" per the HTTP Method Semantics section.

**Decision**: Model the agent interaction as creating a message resource: rename to `POST /scheduling/agent/messages`. This returns `201 Created` with the agent's response, treating each interaction as a created message resource. This satisfies the constitution rule while preserving the conversational semantics.

**Impact**: OpenAPI YAML path change, generated interface rename, controller path update, quickstart curl command update.

### Clock Injection Adaptation

The constitution v1.6.0 added: *"All production code that requires the current time MUST obtain it from the auto-configured `Clock` bean... Direct calls to `Instant.now()` are forbidden in application and infrastructure code."*

The `Show` aggregate constructor calls `Instant.now()` directly to validate that `scheduledAt` is in the future. The domain layer must remain technology-agnostic (no Spring dependency), so `Clock` cannot be injected into the domain.

**Decision**: Pass the current time as an `Instant now` parameter to the `Show` constructor. The application-layer command handler injects the `Clock` bean and passes `clock.instant()` when constructing the Show.

**Impact**:
- `Show.java`: Constructor gains `Instant now` parameter; validation becomes `scheduledAt.isAfter(now)`
- `ShowCommandHandlerImpl.java`: Inject `Clock` via constructor; pass `clock.instant()` to `new Show(...)`
- `ShowFixture.java`: Accept `Instant now` parameter or use a hardcoded fixed instant
- `ShowTest.java`: Use fixed instants instead of `Instant.now()`
- `ShowCommandHandlerImplTest.java`: Mock or construct a fixed `Clock`
- `JpaShowRepositoryTest.java`: Use fixed instant (PersistenceTest can override Clock bean)
- Integration tests: Override `Clock` bean with `Clock.fixed(...)` via test configuration

## Project Structure

### Documentation (this feature)

```text
specs/002-ai-show-scheduling/
├── plan.md              # This file (adapted for constitution v1.6.0)
├── research.md          # Phase 0 output (updated with R6, R7)
├── data-model.md        # Phase 1 output (updated for Clock parameter)
├── quickstart.md        # Phase 1 output (updated for new endpoint path)
├── contracts/           # Phase 1 output (updated OpenAPI, agent tools)
├── checklists/          # Requirements checklist
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
pom.xml
scheduling/
├── src/main/java/com/github/scheduling/
│   ├── application/
│   │   └── show/
│   │       ├── ShowCommandHandler.java          # @Transactional interface
│   │       ├── ShowCommandHandlerImpl.java      # Injects Clock (NEW)
│   │       ├── ShowQueryHandler.java
│   │       ├── HallQueryHandler.java / Impl
│   │       ├── MovieQueryHandler.java / Impl
│   │       ├── command/ (ScheduleShowCommand, ScheduleShowResult)
│   │       └── query/ (views, queries)
│   ├── domain/
│   │   ├── show/ (Show, ShowId, ShowEvent, ShowScheduled, ShowException, ShowRepository, ShowSchedulingPolicy)
│   │   ├── movie/ (Movie, MovieId, MovieService)
│   │   └── hall/ (Hall, HallId, HallService)
│   └── infrastructure/
│       ├── agent/ (SchedulingAgent)
│       ├── event/ (ShowScheduledIntegrationEventPublisher)
│       ├── persistence/show/ (JpaShowRepository, JpaShowQueryHandler, JpaShowSchedulingPolicy)
│       ├── service/ (MockMovieService, MockHallService)
│       └── web/
│           ├── agent/ (AgentController)             # Path updated to /scheduling/agent/messages
│           └── show/ (ShowController, ShowMapper)
├── src/main/resources/
│   ├── META-INF/domain/show.orm.xml
│   ├── META-INF/query/show.orm.xml
│   ├── db/migration/V1_0__show.sql
│   ├── static/scheduling-openapi.yaml               # Path updated
│   ├── application-default.yaml
│   └── application-local.yaml
└── src/test/java/com/github/scheduling/
    ├── ArchitectureTest.java
    ├── domain/show/ (ShowTest, ShowFixture)           # Fixed Clock
    ├── application/show/ (ShowCommandHandlerImplTest)  # Mocked Clock
    └── infrastructure/
        ├── persistence/ (PersistenceTest, JpaShowRepositoryTest, JpaShowQueryHandlerTest)
        └── web/ (ControllerTest, ShowControllerTest)

seedwork/
├── src/main/java/com/github/seedwork/infrastructure/time/
│   └── ClockAutoConfiguration.java                    # Existing — provides Clock.systemUTC()
```

**Structure Decision**: Existing `scheduling` module structure is correct. Changes are localized to: `Show.java` (constructor signature), `ShowCommandHandlerImpl.java` (Clock injection), OpenAPI YAML (path rename), `AgentController.java` (path update), and test files (fixed Clock).

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | All constitution checks pass after adaptation | N/A |
