# Specification: Spring Boot DDD Booking Context Baseline

Feature Branch: `001-project-baseline`
Created: 2026-03-13
Status: Draft
Input: "Define full project specification for tech stack, architecture, and naming conventions."

## Overview

This specification documents the current baseline behavior and architecture of the repository so future changes can
be designed against explicit contracts rather than implicit code knowledge.

The system provides:
- a booking bounded context (`booking` module) with command and query APIs,
- a reusable platform seedwork (`seedwork` module) for problem handling, aggregate persistence support, and outbox.

## Product Scope

### In Scope

- Show read APIs (list/search/get show, list seats).
- Booking command APIs (initiate, reserve/release seat, confirm, cancel).
- Booking read APIs (get/list bookings).
- Ticket APIs (get/list tickets, redeem issued ticket).
- Domain event-driven seat booking/release synchronization.
- Domain event-driven ticket issuance on seat booking.
- Outbox message persistence, locking, requeue/dequeue, and processing.
- Architecture and naming conventions required for long-term maintainability.

### Out of Scope

- Authentication and authorization flows.
- Multi-tenant support.
- External movie/hall provider integrations beyond current mock services.
- Distributed transactions across bounded contexts.

## Primary Actors

- API Consumer: calls booking/show HTTP APIs.
- Application Developer: extends business behavior while preserving architecture constraints.
- Platform Operator: monitors outbox health and retries failed messages.

## User Stories and Acceptance

### Story 1 (P1): Manage a Booking Lifecycle

As an API consumer, I can initiate a booking for a show, reserve/release seats, and confirm/cancel the booking.

Acceptance Criteria:
1. `POST /shows/{show_id}/bookings` returns `201` with a valid `BookingId`.
2. `PUT /bookings/{booking_id}/reserved-seats/{seat_number}` reserves an available seat and returns `204`.
3. `DELETE /bookings/{booking_id}/reserved-seats/{seat_number}` releases a reserved seat and returns `204`.
4. `PUT /confirmed-bookings/{booking_id}` confirms the booking and returns `204`.
5. `DELETE /bookings/{booking_id}` cancels the booking and returns `204`.
6. Invalid state transitions return Problem Details with conflict semantics.

### Story 2 (P1): Discover Shows and Seat Availability

As an API consumer, I can list/search shows and inspect seats for a selected show.

Acceptance Criteria:
1. `GET /shows` returns paginated show summaries.
2. `GET /search/shows?q=...` returns ranked show summaries by title/hall relevance.
3. `GET /shows/{show_id}` returns show details.
4. `GET /shows/{show_id}/seats` returns seat status with optional booking projection.
5. Unknown IDs return Problem Details with not-found semantics.

### Story 3 (P2): Keep Seat State Consistent with Booking Events

As the system, when booking events occur, show seat states are updated consistently.

Acceptance Criteria:
1. On `BookingConfirmed`, all seats assigned to the booking become booked.
2. On `BookingCancelled`, all seats assigned to the booking become available.
3. Event handlers fetch and validate both aggregates before mutation.
4. Persistence writes and event publication occur through aggregate repository support.

### Story 4 (P2): Issue and Redeem Tickets

As an API consumer, I can retrieve issued tickets and redeem them once.

Acceptance Criteria:
1. On `SeatBooked`, a `Ticket` is issued with a stable `TicketId` and seat assignment details.
2. `GET /tickets/{ticket_id}` returns ticket details including booking and seat assignment projection.
3. `GET /tickets` returns paginated ticket summaries.
4. `PUT /redeemed-tickets/{ticket_id}` redeems an issued ticket and returns `204`.
5. Redeeming an already redeemed ticket is idempotent; invalid ticket state changes return Problem Details.

### Story 5 (P2): Operate Outbox Reliably

As an operator, I can inspect and control outbox messages for failure recovery.

Acceptance Criteria:
1. Outbox endpoints provide peek, lock, requeue, and dequeue operations.
2. Polling can be disabled (`outbox.poll-interval=PT0S`) for deterministic tests.
3. Processing honors group ordering and lock duration constraints.
4. Outbox health indicator is available when enabled.

## Functional Requirements

### Domain and Application

- FR-001: Booking aggregate supports states `INITIATED`, `CONFIRMED`, and `CANCELLED`.
- FR-002: Booking confirmation is valid only from `INITIATED`; repeated confirm on confirmed is idempotent.
- FR-003: Booking cancellation is valid only from `INITIATED`; repeated cancel on cancelled is idempotent.
- FR-004: Show aggregate manages seat collection and enforces reservation/release/booking invariants.
- FR-005: Application command handlers orchestrate repositories/services and persist mutated aggregates.
- FR-006: Application query handlers return read models (`*View`) without exposing persistence entities.
- FR-021: Ticket aggregate supports states `ISSUED` and `REDEEMED`.
- FR-022: Ticket redemption is valid only from `ISSUED`; repeated redemption on redeemed ticket is idempotent.
- FR-023: Ticket issuance is triggered by `SeatBooked` domain events.

### API and Error Handling

- FR-007: HTTP API contracts are defined by OpenAPI specs and implemented by generated operations interfaces.
- FR-008: Query pagination uses `offset` (default `0`) and `limit` (default `10`, max `100`).
- FR-009: Validation, malformed requests, and business exceptions return RFC 7807 Problem Details.
- FR-010: Domain/application failures use typed `ProblemException` with stable problem URNs.
- FR-024: Ticket endpoints (`getTicket`, `listTickets`, `redeemTicket`) are part of the same Booking API contract.

### Persistence and Messaging

- FR-011: Aggregate persistence uses JPA with XML mapping resources for domain and query projections.
- FR-012: Schema changes are managed by Flyway migrations in module resources.
- FR-013: Read-side projections are served by named native queries mapped to record constructors.
- FR-014: Aggregate saves publish raised events through the configured event publisher.
- FR-015: Outbox storage tracks sequence number, group id, lock state, attempts, subject, and payload.
- FR-025: Ticket persistence uses dedicated migration (`V1_2__ticket.sql`) and dedicated ORM/query mappings.

### Architecture and Conventions

- FR-016: `booking` layer dependencies follow `domain <- application <- infrastructure`.
- FR-017: `seedwork` layer dependencies follow `core <- domain <- infrastructure`.
- FR-018: Domain classes do not expose JavaBean getters/setters.
- FR-019: Commands and queries use suffixes `*Command` and `*Query`; projections use `*View`.
- FR-020: Repository adapters use `Jpa*Repository` / `Jpa*QueryHandler` naming in infrastructure.
- FR-026: Test classes use `*Test` naming; behavioral `@Test` methods follow lowerCamelCase `...Should...`
  phrasing, while `@ArchTest` methods may use explicit invariant-style names.
- FR-027: Tests follow explicit Arrange/Act/Assert section comments and reuse shared `*Fixture` helpers where possible.

## Non-Functional Requirements

- NFR-001: Build compatibility with Java 25 and Maven 3.9+.
- NFR-002: CI must pass `mvn -B package --file pom.xml` on push and pull request.
- NFR-003: Architecture rules must be enforced via ArchUnit tests.
- NFR-004: Persistence tests must remain isolated and schema-clean between test methods.
- NFR-005: OpenAPI generation must remain reproducible from checked-in YAML specs.
- NFR-006: Outbox processing should support concurrent batch handling while preserving per-group ordering semantics.
- NFR-007: Controller tests must run against `@SpringBootTest` random-port setup via `ControllerTest` base classes.
- NFR-008: Persistence integration tests must run via `PersistenceTest` base classes with Flyway cleanup after each test.

## Technical Baseline

- Language: Java 25
- Framework: Spring Boot 4.0.3
- Build: Maven multi-module (`booking`, `seedwork`)
- Persistence: Spring Data JPA + ORM XML mappings
- Migrations: Flyway
- API Contract: OpenAPI 3 + OpenAPI Generator (interface-only server stubs)
- Database (local/test): H2 in PostgreSQL compatibility mode
- Architecture Testing: ArchUnit
- Runtime Pattern: Domain events + transactional outbox
- Testing Conventions: JUnit 5 `@Test`, ArchUnit `@ArchTest`, `@MockitoBean` for web adapter tests, AAA comments.

## Domain Model Summary

- Booking Aggregate
  - Identity: `BookingId`
  - References: `ShowId`
  - State: `BookingStatus`
  - Events: `BookingInitiated`, `BookingConfirmed`, `BookingCancelled`
- Show Aggregate
  - Identity: `ShowId`
  - Attributes: `scheduledAt`, `movie`, `hall`, `seats`
  - Seat model: `SeatNumber`, `SeatStatus`, optional assigned `BookingId`
  - Events: `SeatReserved`, `SeatReleased`, `SeatBooked`
- Ticket Aggregate
  - Identity: `TicketId`
  - References: `BookingId`
  - State: `TicketStatus`
  - Attributes: `SeatAssignment` (`movieTitle`, `hallName`, `scheduledAt`, `seatNumber`)
  - Events: `TicketIssued`, `TicketRedeemed`
- Supporting Value Objects
  - `MovieId`, `HallId`, `SeatLayout`, `SeatNumber`, `TicketId`, `SeatAssignment`

## Naming Conventions Baseline

- IDs:
  - `BookingId`: `B0[0-9A-F]{16}`
  - `ShowId`: `S0[0-9A-F]{16}`
  - `MovieId`: `M0[0-9A-F]{16}`
  - `HallId`: `H0[0-9A-F]{16}`
  - `TicketId`: `T0[0-9A-F]{16}`
- Layers:
  - `com.github.booking.domain.*`
  - `com.github.booking.application.*`
  - `com.github.booking.infrastructure.*`
- Adapters:
  - Web: `*Controller`, `*Mapper`
  - Persistence: `Jpa*Repository`, `Jpa*QueryHandler`
  - Messaging/service stubs: `Mock*`
- SQL:
  - snake_case tables/columns, explicit `_pk`, `_fk`, `_ui`, `_i` suffixes.

## Success Metrics

- All architecture and unit/integration tests pass in CI.
- API contracts and generated operation interfaces remain stable without manual signature drift.
- New contributors can add a feature without violating layer or naming rules.
- Outbox processing remains observable and operable through API + health checks.
- Ticket issuance and redemption flows remain consistent with booking/show state transitions.

## Open Questions

- None for baseline documentation; future feature specs may refine throughput and operational SLAs.
