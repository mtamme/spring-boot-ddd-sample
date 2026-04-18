# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION]  
**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION]  
**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files or N/A]  
**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIFICATION]  
**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEEDS CLARIFICATION]
**Project Type**: [e.g., library/cli/web-service/mobile-app/compiler/desktop-app or NEEDS CLARIFICATION]  
**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k lines/sec, 60 fps or NEEDS CLARIFICATION]  
**Constraints**: [domain-specific, e.g., <200ms p95, <100MB memory, offline-capable or NEEDS CLARIFICATION]  
**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50 screens or NEEDS CLARIFICATION]

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [ ] Module and layer impact is explicit (`booking`: `infrastructure -> application -> domain`; `seedwork`: `infrastructure -> domain -> core`); domain layer contains only Entity/Event/ValueObject types, @Service domain services, ProblemException subclasses, and interfaces.
- [ ] Domain changes keep business rules in domain types; new aggregates raise creation events in the constructor; `equals()`/`hashCode()` use domain identity, not surrogate key; `private Long id` and `protected` no-arg constructor are at the bottom of each aggregate class.
- [ ] Required automated tests are identified for every touched layer, including ArchUnit when structure changes.
- [ ] OpenAPI specs, Flyway migrations, domain ORM XMLs (`META-INF/domain/`), query projection ORM XMLs (`META-INF/query/`), and generated interfaces are accounted for when contracts or persistence change; ORM resources are explicitly listed in `spring.jpa.mapping-resources`.
- [ ] All precondition checks use `Contract.require()` and all invariant checks use `Contract.check()`; domain exceptions extend `ProblemException` with `<STATE>_PROBLEM` constants; `notFound()` uses `Problem.notFound()` (HTTP 404), invariant violations use `Problem.invariant()` (HTTP 409), precondition violations use `Problem.precondition()` (HTTP 422); idempotent state transitions use an early-return guard after the `Contract.check()`.
- [ ] Naming follows layer conventions: noun accessors and `is<State>()` predicates in domain; `<verb><Noun>(<Command>)` in command handlers; `get<Entity>` / `list<Entities>` / `search<Entities>` in query handlers; `on<EventType>` in event handlers; `<Noun>DetailView` / `<Noun>SummaryView` for view projections; `to<TargetType>()` in `@Component` mappers; `<method>With<State>Should<Behavior>()` in tests; `<Aggregate>Fixture` factory methods in test fixtures.
- [ ] RESTful API design follows conventions: OpenAPI YAML written first; HTTP methods follow POST=create/PUT=idempotent state transition/DELETE=cancel/GET=read; POST MUST target a collection resource URI (e.g., `POST /shows/{show_id}/bookings`), never an individual resource; URL paths use plural kebab-case nouns and snake_case path params; state transitions use state-scoped paths (e.g., `/confirmed-bookings/{id}`); collections use offset-limit pagination (max 100); search uses `GET /search/{resource}?q=`; ID fields declare `<PREFIX>0[0-9A-F]{16}` pattern; responses use 201/204/200; errors use `application/problem+json` (RFC 7807); collection responses wrapped, single responses flat, creation responses return ID only.
- [ ] Cross-aggregate side effects flow exclusively through domain events; no aggregate holds direct references to or invokes behavior on another aggregate; event handlers in the application layer follow load-invoke-save (load target aggregate, invoke domain method, save); events carry only identity values and minimal data; each aggregate defines an abstract base event class implementing seedwork `Event`. Reactive aggregate creation (one aggregate acting as factory for another, e.g., `Show.issueTicket(...)`) persists the new aggregate through the target aggregate's own repository so the target's creation event is published.
- [ ] Event publication uses the seedwork transactional outbox only: aggregates `raiseEvent(...)`, repositories route through `JpaAggregateRootSupport.saveAndPublishEvents`, and no code calls `ApplicationEventPublisher.publishEvent` directly or introduces a parallel publication channel; event payloads are `Serializable` records holding only identity values and primitives; because outbox delivery is at-least-once, every `@EventListener` is idempotent and relies on aggregate state guards rather than delivery-state tracking; external systems MUST NOT integrate via the outbox messages (the REST API at `/outbox/messages` defined in `seedwork/static/outbox/outbox-openapi.yaml` is operational-only — lookup current messages, requeue failed messages); out-of-process integration flows through a dedicated in-process outbox consumer (`@EventListener`) in the owning bounded context that forwards dispatched events to the target external channel.
- [ ] CQRS separation maintained: command handlers accept command records and follow load-invoke-save with no business logic in handlers; query handlers return view projections via dedicated named native queries without loading full aggregates; command/query/result/view records organized in `command/` and `query/` sub-packages; infrastructure mirrors with `persistence/<aggregate>/` and `web/<aggregate>/`.
- [ ] Test infrastructure: persistence tests extend `PersistenceTest` (separate read/write transactions); controller tests extend `ControllerTest` with `@MockitoBean` handler isolation; application-layer tests mock repository dependencies.
- [ ] Clock access uses the auto-configured `Clock` bean via constructor injection; no direct `Instant.now()`, `LocalDateTime.now()`, or `Clock.systemUTC()` calls; tests use a fixed `Clock` (unit tests construct/mock it, integration tests override the bean).

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
pom.xml
booking/
├── src/main/java/com/github/booking/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── src/main/resources/
│   ├── META-INF/
│   ├── db/migration/
│   └── static/
└── src/test/java/com/github/booking/
    ├── application/
    ├── domain/
    ├── infrastructure/
    └── ArchitectureTest.java

seedwork/
├── src/main/java/com/github/seedwork/
│   ├── core/
│   ├── domain/
│   └── infrastructure/
├── src/main/resources/seedwork/
│   ├── META-INF/
│   ├── db/migration/outbox/
│   └── static/outbox/
└── src/test/java/com/github/seedwork/
    ├── domain/
    ├── infrastructure/
    └── ArchitectureTest.java
```

**Structure Decision**: [Document affected modules, layers, and resource
directories from the tree above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
