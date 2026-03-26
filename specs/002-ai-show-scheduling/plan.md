# Implementation Plan: AI Show Scheduling Bounded Context

**Branch**: `002-ai-show-scheduling` | **Date**: 2026-03-26 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-ai-show-scheduling/spec.md`

## Summary

Introduce a new `scheduling` bounded context as a separate Maven module that enables AI-powered show scheduling via the Claude LLM API. The Show aggregate enforces scheduling invariants (future-date, no hall-time overlap). An infrastructure-layer AI agent exposes application-layer command/query handlers as Claude tools. Hall and movie data are provided by mock domain services. A `ShowScheduled` domain event is converted to an integration event and published via a mocked infrastructure publisher.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0.4, Spring Data JPA, Flyway, Anthropic Java SDK 2.18.0, H2 (local)
**Storage**: H2 in-memory (local profile), PostgreSQL-compatible SQL, Flyway migrations
**Testing**: JUnit 5, Mockito, ArchUnit 1.4.1, Spring Boot Test (PersistenceTest, ControllerTest bases from seedwork)
**Target Platform**: Spring Boot web application (local development)
**Project Type**: Multi-module Maven web-service (new `scheduling` module)
**Performance Goals**: Command handler invocations < 5 seconds (excluding LLM response time), p95 < 200ms for read endpoints
**Constraints**: No dependency on `booking` module; domain layer technology-agnostic; ORM XML only (no JPA annotations); OpenAPI-first controllers
**Scale/Scope**: Single new module with ~30-40 source files across domain/application/infrastructure layers + tests

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Module and layer impact is explicit (`scheduling`: `infrastructure -> application -> domain`; depends on `seedwork` only); domain layer contains only Entity/Event/ValueObject types, @Service domain services, ProblemException subclasses, and interfaces.
- [x] Domain changes keep business rules in domain types; Show aggregate raises `ShowScheduled` event in the constructor; `equals()`/`hashCode()` use `showId` domain identity; `private Long id` and `protected` no-arg constructor at bottom of aggregate class.
- [x] Required automated tests are identified for every touched layer, including ArchUnit for the new `scheduling` module structure.
- [x] OpenAPI spec (`scheduling-openapi.yaml`) defined for REST endpoints; Flyway migration for `show` table; domain ORM XML (`META-INF/domain/show.orm.xml`); query projection ORM XML (`META-INF/query/show.orm.xml`); all resources listed in `spring.jpa.mapping-resources`.
- [x] Performance budget documented: read endpoints target p95 < 200ms; command handler < 5s excluding LLM; no N+1 risks (single aggregate, no collections); query handlers use explicit native queries with OFFSET/FETCH pagination.
- [x] All preconditions use `Contract.require()`; invariants use `Contract.check()`; `ShowException` extends `ProblemException` with `OVERLAP_PROBLEM` (conflict/409), `PAST_SCHEDULE_PROBLEM` (conflict/409), `NOT_FOUND_PROBLEM` (notFound/404).
- [x] Naming follows conventions: `showId()` accessor, `scheduleShow(ScheduleShowCommand)` command handler, `getShow`/`listShows` query handlers, `ShowDetailView`/`ShowSummaryView` projections, `ShowFixture` test fixture.
- [x] RESTful API design follows conventions: OpenAPI YAML written first; `GET /shows/{show_id}` and `GET /shows` with offset/limit pagination; `POST /scheduling/agent` for agent interaction; `application/problem+json` error responses.
- [x] Cross-aggregate side effects flow through domain events; `ShowScheduled` raised in constructor, published via `saveAndPublishEvents`; integration event publication is an infrastructure listener, not cross-aggregate orchestration.
- [x] CQRS maintained: `ShowCommandHandler`/`ShowCommandHandlerImpl` for writes; `ShowQueryHandler`/`JpaShowQueryHandler` for reads; `HallQueryHandler`/`MovieQueryHandler` for mock data reads; command/query/view records in sub-packages.
- [x] Test infrastructure: persistence tests extend `PersistenceTest`; controller tests extend `ControllerTest` with `@MockitoBean`; application-layer tests mock repositories; fixtures clear events via `releaseEvents(Consumers.empty())`.

## Project Structure

### Documentation (this feature)

```text
specs/002-ai-show-scheduling/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── scheduling-openapi.yaml
│   └── agent-tools.md
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
pom.xml                          # Add <module>scheduling</module>

scheduling/
├── pom.xml                      # New module POM
├── src/main/java/com/github/scheduling/
│   ├── domain/
│   │   ├── show/
│   │   │   ├── Show.java                    # Aggregate root
│   │   │   ├── ShowId.java                  # Value object
│   │   │   ├── ShowEvent.java               # Abstract base event
│   │   │   ├── ShowScheduled.java           # Domain event
│   │   │   ├── ShowRepository.java          # Repository interface
│   │   │   ├── ShowSchedulingPolicy.java    # Domain service interface
│   │   │   └── ShowException.java           # Problem exception
│   │   ├── movie/
│   │   │   ├── Movie.java                   # Value object
│   │   │   ├── MovieId.java                 # Value object
│   │   │   └── MovieService.java            # Domain service interface
│   │   └── hall/
│   │       ├── Hall.java                    # Value object
│   │       ├── HallId.java                  # Value object
│   │       └── HallService.java             # Domain service interface
│   ├── application/
│   │   └── show/
│   │       ├── ShowCommandHandler.java      # Command handler interface
│   │       ├── ShowCommandHandlerImpl.java  # Implementation
│   │       ├── ShowQueryHandler.java        # Query handler interface
│   │       ├── HallQueryHandler.java        # Query handler interface
│   │       ├── MovieQueryHandler.java       # Query handler interface
│   │       ├── command/
│   │       │   ├── ScheduleShowCommand.java
│   │       │   └── ScheduleShowResult.java
│   │       └── query/
│   │           ├── GetShowQuery.java
│   │           ├── ListShowsQuery.java
│   │           ├── ListHallsQuery.java
│   │           ├── ListMoviesQuery.java
│   │           ├── ShowDetailView.java
│   │           ├── ShowSummaryView.java
│   │           ├── HallView.java
│   │           └── MovieView.java
│   └── infrastructure/
│       ├── agent/
│       │   └── SchedulingAgent.java         # Claude LLM agent with tools
│       ├── event/
│       │   └── ShowScheduledIntegrationEventPublisher.java  # Mock integration event
│       ├── persistence/
│       │   └── show/
│       │       ├── JpaShowRepository.java
│       │       ├── JpaShowQueryHandler.java
│       │       └── JpaShowSchedulingPolicy.java
│       ├── service/
│       │   ├── hall/
│       │   │   └── MockHallService.java
│       │   └── movie/
│       │       └── MockMovieService.java
│       └── web/
│           ├── agent/
│           │   ├── AgentController.java
│           │   └── AgentMapper.java
│           └── show/
│               ├── ShowController.java
│               └── ShowMapper.java
├── src/main/resources/
│   ├── META-INF/
│   │   ├── domain/
│   │   │   └── show.orm.xml
│   │   └── query/
│   │       └── show.orm.xml
│   ├── db/migration/
│   │   └── V1_0__show.sql
│   ├── static/
│   │   └── scheduling-openapi.yaml
│   ├── application-default.yaml
│   └── application-local.yaml
└── src/test/java/com/github/scheduling/
    ├── domain/
    │   ├── show/
    │   │   ├── ShowTest.java
    │   │   └── ShowFixture.java
    │   ├── movie/
    │   │   └── MovieFixture.java
    │   └── hall/
    │       └── HallFixture.java
    ├── application/
    │   └── show/
    │       └── ShowCommandHandlerImplTest.java
    ├── infrastructure/
    │   ├── persistence/
    │   │   ├── PersistenceTest.java           # Extends seedwork PersistenceTest
    │   │   └── show/
    │   │       ├── JpaShowRepositoryTest.java
    │   │       └── JpaShowQueryHandlerTest.java
    │   └── web/
    │       ├── ControllerTest.java             # Extends seedwork ControllerTest
    │       └── show/
    │           └── ShowControllerTest.java
    └── ArchitectureTest.java
```

**Structure Decision**: New `scheduling` module added to root POM `<modules>`. Module follows identical layer structure as `booking`. Domain layer is technology-agnostic. Infrastructure contains Claude SDK integration, JPA persistence, mock services, and REST controllers. AI agent is an infrastructure component that delegates to application-layer handlers.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Agent REST endpoint (`POST /scheduling/agent`) is not a standard CRUD resource | AI agent interaction is conversational, not CRUD — a single endpoint accepting natural-language messages is the most natural API shape | Splitting into separate endpoints per action would defeat the purpose of AI-driven natural language scheduling |
| Integration event listener in infrastructure layer (not application layer) | Integration event publication is a cross-context infrastructure concern, not cross-aggregate domain orchestration — user explicitly requested infrastructure placement | Application-layer event handler would misrepresent the intent; this is not load-invoke-save, it's event forwarding |
