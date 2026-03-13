# Tasks: Spring Boot DDD Booking Context Baseline

## Phase 1: Foundation

- [ ] T001 Validate architecture tests in `booking` and `seedwork` pass unchanged.
- [ ] T002 Confirm OpenAPI-generated operations interfaces compile cleanly against controllers.
- [ ] T003 Confirm Flyway migrations and ORM mappings are synchronized for booking, show, and ticket.

## Phase 2: Domain and Application Quality

- [ ] T004 Verify booking lifecycle invariants and idempotency tests are comprehensive.
- [ ] T005 Verify show seat reservation/release/booking invariants and test coverage.
- [ ] T006 Verify ticket issuance/redemption invariants and event-driven issuance from `SeatBooked`.
- [ ] T007 Verify application handlers only orchestrate repositories/services and keep logic in domain.

## Phase 3: Infrastructure and Contract Stability

- [ ] T008 Verify booking/show/ticket query projections (`*View`) match named native SQL result mappings.
- [ ] T009 Verify Problem Details mapping for validation, not-found, conflict, and unexpected failures.
- [ ] T010 Verify ID format conventions remain stable across domain and OpenAPI schemas (`B0`, `S0`, `M0`, `H0`, `T0`).

## Phase 4: Eventing and Outbox Reliability

- [ ] T011 Verify aggregate save paths consistently use `saveAndPublishEvents(...)`.
- [ ] T012 Verify outbox lock/requeue/dequeue behavior and health indicator behavior under tests.
- [ ] T013 Verify event handlers maintain booking/show consistency on confirmation and cancellation events.
- [ ] T014 Verify ticket event handler issues tickets on seat booking and remains transactionally consistent.

## Phase 5: Release Gates

- [ ] T015 Run `mvn clean package` and resolve all regressions.
- [ ] T016 Confirm test naming/style conventions are preserved (`*Test`, behavioral `...Should...`, `@ArchTest`
  invariant naming, Arrange/Act/Assert comments).
- [ ] T017 Confirm constitution compliance checklist is complete for changed areas.
- [ ] T018 Document any deviations as explicit constitution amendment proposals.
