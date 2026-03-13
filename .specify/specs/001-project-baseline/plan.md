# Implementation Plan: Spring Boot DDD Booking Context Baseline

Branch: `001-project-baseline`
Date: 2026-03-13
Spec: `.specify/specs/001-project-baseline/spec.md`
Constitution: `.specify/memory/constitution.md`

## Summary

This plan defines how to implement and validate changes in this repository while preserving the documented baseline:
domain-centric layering, contract-first HTTP APIs, explicit domain modeling (booking/show/ticket), and outbox-backed
event delivery.

## Technical Context

- Language/Version: Java 25
- Build Tool: Maven 3.9+
- Framework: Spring Boot 4.0.3
- Modules: `booking` (bounded context), `seedwork` (shared platform building blocks)
- Persistence: Spring Data JPA with XML ORM mappings and named native SQL queries
- Database: H2 (PostgreSQL compatibility mode for local/test)
- Schema Migration: Flyway
- API Contract: OpenAPI 3 specs + OpenAPI Generator (`interfaceOnly=true`)
- Messaging Pattern: Domain events + transactional outbox
- Testing: JUnit 5, Spring Boot Test, ArchUnit, Mockito
- Test Naming: class `*Test`, behavioral `@Test` lowerCamelCase `...Should...`, explicit AAA comments, invariant-style
  `@ArchTest` method names
- CI Gate: `mvn -B package --file pom.xml`

## Constitution Compliance Check

1. Domain-Centric Architecture
- Check ArchUnit layer tests for both modules before merge.
- Reject any dependency that points outward from domain/application core.

2. Domain Model Integrity
- Enforce domain-only concepts in `*.domain..`.
- Validate immutable value objects/events and no JavaBean getters/setters in domain types.

3. Application Semantics
- Keep use-case orchestration in handlers.
- Keep commands/queries as explicit records; avoid infrastructure leakage.

4. Infrastructure Adapter Rules
- Controllers implement generated `*Operations` only.
- Mappers stay pure mapping; persistence adapters hold SQL/JPA specifics.

5. Eventing and Outbox
- Aggregate saves use `saveAndPublishEvents`.
- Verify outbox ordering and lock behavior in persistence/outbox tests.

6. API and Error Contract
- Regenerate OpenAPI stubs when spec changes.
- Ensure `ProblemException` flows to RFC 7807 responses.

7. Naming and Structure
- Enforce `*Command`, `*Query`, `*View`, `*Id`, `*Exception`, `Jpa*Repository` naming.
- Preserve ID format regex compatibility, including `TicketId`.

8. Testing and Delivery
- Keep unit, integration, and architecture tests updated for each change set.
- Keep existing test naming and style conventions (`*Test`, behavioral `...Should...`, invariant-style `@ArchTest`,
  Arrange/Act/Assert comments).
- Require packaging build success prior to review completion.

## Project Structure Strategy

### Booking Module

- `domain/*`: booking/show/ticket aggregates, value objects, events, repository/service interfaces, domain exceptions.
- `application/*`: booking/show/ticket command-query interfaces, handlers, records, event handlers.
- `infrastructure/web/*`: OpenAPI adapter controllers and mappers for booking/show/ticket resources.
- `infrastructure/persistence/*`: JPA repositories and query handlers for booking/show/ticket read-write paths.
- `infrastructure/service/*`: external service adapters (mock or real implementations).
- `infrastructure/messaging/*`: inbound message listeners.

### Seedwork Module

- `core/*`: framework-agnostic utilities and problem primitives.
- `domain/*`: shared domain abstractions (`Entity`, `ValueObject`, `Event`, `AggregateRoot`, `Contract`).
- `infrastructure/*`: outbox, persistence support, web problem handling, auto-configuration.

## Phased Delivery Workflow

### Phase 0: Alignment and Safety

- Confirm target scope against `.specify/specs/001-project-baseline/spec.md`.
- Identify impacted layers and modules.
- Define required tests before implementation.

Exit Criteria:
- Scope and invariants are explicit.
- No ambiguity on affected contracts.

### Phase 1: Domain and Application Changes

- Implement or adjust domain behavior first (including ticket issuance/redemption invariants).
- Add/update command/query records and handlers.
- Add/update domain and application tests.

Exit Criteria:
- Domain invariants are explicit and tested.
- Application orchestration compiles without adapter changes.

### Phase 2: Infrastructure Adapters

- Update persistence mappings/queries and adapters.
- Update controller/mappers against generated OpenAPI interfaces.
- Keep adapter code thin and layer-compliant.

Exit Criteria:
- Adapter tests pass.
- No architecture rule regression.

### Phase 3: Eventing and Outbox

- Verify raised events are published via repository support.
- Verify `SeatBooked` to ticket issuance flow (`TicketEventHandler`) remains consistent.
- Update outbox behavior only through seedwork abstractions.
- Add/adjust outbox tests for lock, retry, and dequeue/requeue behavior as needed.

Exit Criteria:
- Event-driven flows are deterministic under tests.
- Outbox processing behavior remains operationally observable.

### Phase 4: Verification and Release Readiness

- Run full build and test suite.
- Regenerate OpenAPI artifacts when API specs changed.
- Validate migration naming/order and compatibility.

Exit Criteria:
- `mvn clean package` succeeds.
- All constitution checks are satisfied.

## Risk Register

- Risk: Layer leakage from infrastructure into domain/application.
  Mitigation: ArchUnit checks + PR review checklist tied to constitution.

- Risk: API signature drift between OpenAPI and controllers.
  Mitigation: generated interfaces remain authoritative; compile-time enforcement.

- Risk: Outbox retry/lock regressions.
  Mitigation: preserve outbox integration tests and lock semantics.

- Risk: SQL projection mismatch for record constructors.
  Mitigation: verify named query mappings and run persistence tests.

- Risk: booking/show/ticket event-flow inconsistency (e.g., seat booked without ticket issuance).
  Mitigation: keep application event-handler tests and persistence query-handler tests aligned with domain events.

## Definition of Done

- Scope implemented according to `.specify/specs/001-project-baseline/spec.md`.
- No constitution article violations.
- Required tests added/updated and passing.
- Test naming/style conventions are preserved across newly added tests.
- CI-equivalent build passes locally.
- Any architecture-impacting decision documented with rationale.
