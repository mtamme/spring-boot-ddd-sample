<!--
Sync Impact Report
- Version change: 4.1.0 -> 4.1.1
- PATCH bump rationale: Corrects an inaccurate wording in the § Naming
  Reference for domain-service methods. The previous phrasing
  `get<Entity>(<IdType>)` wrongly implied the returned type is an entity; in
  practice the returned type is most commonly a DDD value object projected
  from a foreign bounded context via an Anti-Corruption Layer (e.g., the
  existing `HallService.getHall(HallId)` returns a `Hall` value object), and
  in rare cases an aggregate root of the owning bounded context. No new
  rule is introduced; no principle is added, removed, or redefined; no
  prohibition is weakened. The fix also makes explicit that returning an
  aggregate-owned child entity is not permitted — a guarantee already
  implied by Principle II (aggregates are reachable only through their
  root) but not previously stated in the Naming Reference.
- Modified principles: none.
- Modified sections:
  - Engineering Standards -> Naming Reference, "Domain layer — domain
    service methods" clause: pattern renamed from `get<Entity>(<IdType>)`
    to `get<Type>(<IdType>)`; the paragraph now names the value-object
    (ACL) case as the common form, the aggregate-root case as a rare but
    permitted form requiring review justification, and explicitly excludes
    aggregate-owned child entities.
- Added sections: none.
- Removed sections: none.
- Templates requiring updates:
  - ✅ .specify/templates/plan-template.md — the Constitution Check bullet
        at line 39 uses `get<Entity>` only in the query-handler scope,
        which is unrelated to this clarification and remains correct.
  - ✅ .specify/templates/spec-template.md — no references affected.
  - ✅ .specify/templates/tasks-template.md — no references affected.
  - ✅ no commands directory present at .specify/templates/commands.
  - ✅ README.md — no principle references; no edits required.
- Follow-up TODOs: none.
-->

# Spring Boot DDD Sample Constitution

## Scope

This constitution defines the architectural, design, and technology rules that
apply to **every bounded context** in this repository. The currently-implemented
`booking` bounded context is the reference implementation and the source of the
concrete examples (aggregates such as `Booking`, `Show`, and `Ticket`; events
such as `BookingConfirmed` and `SeatBooked`; exceptions such as
`BookingException::notFound`). The illustrative references to `booking`
throughout this document MUST be read as "for example"; the enforcement rules
apply identically to every future bounded context added alongside `booking`.

## Core Principles

The key words **MUST**, **MUST NOT**, **SHOULD**, **SHOULD NOT**, and **MAY**
in this document are to be interpreted as described in RFC 2119. "Forbidden"
and equivalent prose wordings are not used; prohibitions are expressed as
"MUST NOT".

Principles are grouped from the highest architectural level down to the most
concrete technology conventions. Higher-level principles constrain lower-level
ones; a lower-level rule never overrides a higher-level one. Where a principle
cross-references another, the roman numeral refers to the numbering in this
document (the plan-template's Constitution Check items do the same).

### Architecture

*System-shape concerns: module layering, cross-aggregate integration, and
event delivery at the system boundary.*

#### I. Domain-Centric Boundaries

All production code MUST live inside explicit inward-facing layers. Every
bounded-context module MUST follow `infrastructure -> application -> domain`;
the shared `seedwork` module MUST follow `infrastructure -> domain -> core`.
Domain classes MUST remain technology-agnostic and MUST NOT depend on web,
persistence, messaging, or framework-specific delivery concerns beyond the
narrowly approved Spring stereotypes already enforced by architecture tests.

The domain layer MUST contain only the following concept types: classes
implementing `Entity`, `Event`, or `ValueObject`; `@Service`-annotated domain
service classes; `ProblemException` subclasses; and interfaces. No other class
types are permitted in the domain layer.

**Rationale:** the repository is structured around DDD modules and ArchUnit
rules; architectural drift would break the sample's primary teaching goal and
would silently spread to every future bounded context that copies existing
patterns.

**Enforcement:** ArchUnit layer-dependency tests in
`booking/src/test/java/.../ArchitectureTest.java` and
`seedwork/src/test/java/.../ArchitectureTest.java`; pull-request review.

#### II. Aggregates Communicate Exclusively Through Domain Events

An aggregate MUST NOT hold direct object references to, or invoke behavior on,
another aggregate. Cross-aggregate references MUST use identity value objects
only (e.g., a `Booking` holds a `ShowId`, not a `Show` reference). Cross-
aggregate side effects MUST be triggered exclusively through domain events:
the originating aggregate raises an event, its repository publishes the event
on save via `saveAndPublishEvents`, and an application-layer event handler
coordinates the reaction on the target aggregate.

Event handlers MUST follow the load-invoke-save pattern: load the target
aggregate(s) from repositories, invoke a single domain method, and save the
aggregate back through its repository (which publishes any further events
raised by the target). Event handlers MUST NOT contain domain business logic;
orchestrating load-invoke-save is the application layer's concern, but all
domain rules and invariants MUST reside exclusively in the aggregate's domain
methods.

An aggregate MAY act as a factory for a different aggregate when it holds the
context required to populate the new aggregate's initial state (e.g.,
`Show.issueTicket(ticketId, seatNumber)` returns a new `Ticket` aggregate
populated from the show's seat assignment). In that case the event handler
MUST (1) load the source aggregate, (2) obtain the next identity from the
target aggregate's repository, (3) call the factory method on the source
aggregate, and (4) save the returned aggregate through the **target
aggregate's** repository so that its creation event is published through the
outbox. The source aggregate MUST NOT persist the newly created aggregate and
MUST NOT retain a reference to it after returning.

Each aggregate MUST define an abstract base event class (e.g., `BookingEvent`,
`ShowEvent`, `TicketEvent`) implementing the seedwork `Event` interface. All
concrete events for that aggregate MUST extend the base class. Event fields
MUST be final and validated via `Contract.require()` in the base constructor.
Events MUST carry only identity values and the minimal primitive data required
by listeners; they MUST NOT embed full aggregate state or mutable references.

**Rationale:** the booking bounded context orchestrates its full
booking-confirmation → seat-booking → ticket-issuance flow entirely through
events and application-layer event handlers; enforcing this pattern for every
future bounded context prevents coupling between aggregates and keeps each
aggregate's transactional boundary independent.

**Enforcement:** ArchUnit aggregate-isolation rules; domain unit tests;
pull-request review.

#### III. Reliable Event Publication via Transactional Outbox (RESTful EDA)

Domain events MUST be published exclusively through the seedwork transactional
outbox. Direct calls to `ApplicationEventPublisher.publishEvent(...)` from
aggregates, domain services, application handlers, or infrastructure code
MUST NOT be made; publication happens only as a side effect of
`saveAndPublishEvents(aggregateId, aggregate)` inside a JPA repository that
extends `JpaAggregateRootSupport`. The implementation delegates to
`OutboxEventPublisher`, which writes each raised event as a `Message` row in
the outbox table within the same transaction as the aggregate state change.
This co-commit guarantee is the contract — any publication path that can
succeed without the aggregate transaction committing, or commit the aggregate
without enqueuing the event, MUST be rejected in review.

Domain events MUST be `Serializable` (already enforced by the seedwork `Event`
interface, which extends `Serializable`) because outbox messages are persisted
as blobs and later rehydrated for dispatch. Event fields MUST therefore be
limited to identity value objects and JDK-serializable primitives/records; no
entity references, JPA proxies, or framework types.

Delivery is at-least-once: `OutboxPoller` periodically drains locked batches,
dispatches each message body via Spring's `ApplicationEventPublisher`, and
marks successfully-dispatched rows as dequeued. Failed dispatches remain in
the outbox and are retried (attempt count is tracked per message). Because
retries can re-invoke any `@EventListener`, every event handler MUST be
idempotent: handler bodies MUST rely on aggregate state guards (see
Principle IV — idempotent state transitions with early-return after
`Contract.check()`) rather than assuming exactly-once semantics. Handlers MUST
NOT perform non-idempotent external side effects without a deduplication key
derived from the event.

External systems MUST NOT integrate via the outbox messages. The REST outbox
API defined by the seedwork OpenAPI document at
`seedwork/src/main/resources/seedwork/static/outbox/outbox-openapi.yaml` and
implemented by `MessageController` under `/outbox/messages` is an
**operational tool only**; its sole purposes are (1) looking up current
messages for inspection and diagnostics and (2) requeuing failed messages for
retry. It is NOT a consumer-facing integration channel, and external systems
MUST NOT poll it to drive business integration:

- `GET /outbox/messages` and `GET /outbox/messages/{sequence_number}` —
  non-destructive lookup of current messages for inspection and diagnostics
  (offset/limit pagination applies); intended for operators and internal
  tooling, not for external application consumers.
- `POST /outbox/messages/locks` — lease the next batch of failed messages
  for operational recovery (returns a `lockId` and the leased messages).
- `PUT /outbox/messages/{sequence_number}/locks/{lock_id}` — requeue a
  leased failed message for retry (negative acknowledgement).
- `DELETE /outbox/messages/{sequence_number}/locks/{lock_id}` —
  acknowledge a leased message as handled (dequeue); reserved for manual
  disposition of poison messages by operators, never as the acknowledgement
  path of an external application consumer.

External consumers MUST NOT read the outbox database table directly and MUST
NOT poll the outbox REST API as a business integration path. Out-of-process
integration with external systems MUST be implemented through a dedicated
**in-process outbox consumer** — an `@EventListener` in the owning bounded
context that reacts to events already dispatched by `OutboxPoller` via
`ApplicationEventPublisher` and forwards them to the target external channel
(message broker, webhook, or similar). That listener inherits the outbox's
at-least-once delivery guarantee and is therefore the authoritative bridge
between the bounded context and any outside system. New bounded contexts MUST
NOT introduce parallel publication channels (message broker clients, direct
webhook POSTs from aggregates or application handlers, etc.); such channels
MUST be driven by an in-process outbox consumer, never by handlers bypassing
the outbox.

**Rationale:** persisting events alongside aggregate state in one transaction
is what turns "events were raised" into "events will be delivered" without
distributed-transaction machinery. The REST outbox API exists so operators
can inspect and recover the event log — requeueing failures and diagnosing
stuck messages — without touching the database; it is an operational surface,
not a public integration contract. Bridging domain events to external systems
is always the responsibility of a dedicated in-process outbox consumer that
knows how to map a domain event to the target external channel; this keeps
integration code inside the bounded context that owns the events and keeps
the outbox API a stable operational surface rather than a shifting consumer
contract.

**Enforcement:** pull-request review; integration tests in `PersistenceTest`
exercise the real outbox path; the seedwork `V0_0__outbox.sql` schema is
owned by seedwork and MUST NOT be altered by bounded-context migrations.

### Design

*Modeling concerns: how aggregates, invariants, and external contracts are
designed within the architectural frame above.*

#### IV. Domain Behavior Is Encapsulated

Business rules MUST be implemented in entities, value objects, domain
services, and domain events rather than in controllers, repositories, or
transport DTOs. Entities, events, and value objects MUST be modeled as
separate concepts; events and value objects MUST be immutable; entity state
MUST NOT be exposed through JavaBean getters or setters.

Domain events representing creation facts MUST be raised in the aggregate
constructor immediately after initial state is set (e.g., a `Booking`
constructor calls `raiseEvent(new BookingInitiated(...))`). Raising creation
events in factory or service methods MUST NOT be done.

Every entity and aggregate MUST implement `equals()` and `hashCode()` using
its domain identity value object (e.g., `bookingId`, `showId`, `ticketId`;
`<AggregateName>Id` in future contexts). The JPA surrogate key (`private Long
id`) MUST NOT participate in equality or hash computation.

Preconditions and invariants MUST be expressed through domain contracts and
problem types so invalid states fail fast and consistently.

Aggregate state transitions MUST be idempotent where the target state is
already reached: after verifying via `Contract.check()` that the precondition
allows the transition, an early-return guard MUST silently exit when the
aggregate is already in the target state, so that repeat invocations are safe
and raise no spurious events. Example: `Booking.confirm()` allows `INITIATED`
or `CONFIRMED` as valid preconditions and returns immediately if already
`CONFIRMED`.

**Rationale:** the codebase uses contract checks, explicit event publication,
and rich domain models instead of anemic records; idempotent transitions
prevent duplicate events and make commands safe to retry — a property that is
load-bearing under the at-least-once outbox delivery required by Principle
III.

**Enforcement:** domain unit tests covering idempotent state transitions;
pull-request review; the `<Aggregate>Fixture` conventions (§ Naming
Reference) make regressions visible in test diffs.

#### V. Code Contracts Enforce Invariants at Every Boundary

All precondition and invariant checks MUST use `Contract.require()` and
`Contract.check()` from `seedwork`. No `if`-then-throw guards, assertion
frameworks, or ad hoc null checks are permitted as replacements.

- `Contract.require(condition)` MUST be used at method entry to validate
  caller obligations (non-null arguments, valid parameter ranges). Violation
  throws `IllegalArgumentException`.
- `Contract.require(condition, exceptionSupplier)` MUST be used when a
  precondition failure requires a typed domain exception.
- `Contract.check(condition)` MUST be used to assert domain state invariants
  mid-method. Violation throws `IllegalStateException`.
- `Contract.check(condition, exceptionSupplier)` MUST be used when an
  invariant violation requires a typed domain exception (e.g.,
  not-confirmable, not-cancelable, not-redeemable).

Resource lookup misses MUST be resolved via `Optional.orElseThrow()` with an
exception factory method reference (e.g.,
`repository.findByBookingId(id).orElseThrow(BookingException::notFound)`).
This applies in both domain code (e.g., `Show.seat()` resolving a child
entity by seat number) and infrastructure query handlers (e.g.,
`JpaBookingQueryHandler.getBooking()`), and by extension in every future
bounded context.

Domain exceptions MUST extend `ProblemException`. Each error case MUST be
represented by a `public static final Problem` constant named
`<SCREAMING_SNAKE_CASE>_PROBLEM` (e.g., `NOT_FOUND_PROBLEM`,
`NOT_CANCELABLE_PROBLEM`, `NOT_REDEEMABLE_PROBLEM`). Resource-not-found
errors MUST use `Problem.notFound()` which maps to HTTP 404; business-state
invariant violations MUST use `Problem.invariant()` which maps to HTTP 409;
precondition violations MUST use `Problem.precondition()` which maps to
HTTP 422. Instances MUST be created only through `public static` factory
methods whose names match the problem suffix in lowerCamelCase (e.g.,
`notFound()`, `notCancelable()`, `notRedeemable()`) — constructors MUST be
private. Exception factory methods MUST be passable as method references so
they can be used directly in `Contract.check(condition,
<Domain>Exception::notCancelable)` (e.g.,
`BookingException::notCancelable`).

**Rationale:** uniform contract usage makes invariant enforcement visible and
grep-able; the `ProblemException` hierarchy ensures every failure carries an
HTTP-mappable problem descriptor without leaking implementation detail.

**Enforcement:** ArchUnit rule requiring domain exceptions to extend
`ProblemException`; pull-request review; grep-level uniformity of
`Contract.require`/`Contract.check` usage.

#### VI. Contracts, Migrations, and Generated Interfaces Stay Aligned

Public HTTP behavior MUST be defined through versioned OpenAPI documents
under module resources, and generated interfaces MUST be treated as derived
artifacts from those specs rather than hand-edited sources. Persistent model
changes MUST be delivered through Flyway migrations and matching ORM/query
mappings; ad hoc schema mutation or `ddl-auto` driven design MUST NOT be
used. When a change affects events, outbox processing, or API
representations, the corresponding contract, mapping, migration, and
automated tests MUST be updated in the same change.

**Rationale:** the repository already ships OpenAPI specs, generated
interfaces, ORM mappings, and Flyway migrations as the canonical integration
contracts; every new bounded context inherits this obligation from day one.

**Enforcement:** `openapi-generator-maven-plugin` regenerates interfaces on
every build; Flyway migration gate (`spring.jpa.hibernate.ddl-auto=none`);
pull-request review.

### Technology

*Concrete tool- and framework-level rules: the specific names, annotations,
and test infrastructure conventions that make the architecture and design
above grep-able and enforceable.*

#### VII. Naming Conventions Are Uniformly Applied Across All Layers

All names — in domain types, application handlers, infrastructure adapters,
tests, fixtures, and ORM/OpenAPI artifacts — MUST follow the conventions
enumerated in § Engineering Standards -> Naming Reference. Reviewers MUST
reject names that diverge from that reference. Any change that introduces a
new naming pattern MUST be reflected in the Naming Reference in the same
pull request.

**Rationale:** consistent naming lets readers locate tests by method name
and predict behavior before reading the body; grep-able conventions keep
test triage, fixture reuse, and onboarding cheap; centralizing the table in
one reference section prevents drift between principles and the patterns
actually used in code.

**Enforcement:** pull-request review against § Naming Reference; existing
ArchUnit conventions where applicable (e.g., handler class suffixes).

#### VIII. Tests Are Mandatory Quality Gates

Every change MUST include the narrowest automated tests that prove the
behavior and the boundary it touches. Domain behavior changes MUST add or
update unit tests in `src/test/java`; application orchestration changes MUST
add service or event-handler tests; web and persistence changes MUST add
Spring Boot integration tests; structural changes MUST keep ArchUnit tests
passing. No feature is complete until `mvn -B package --file pom.xml` passes
locally or the author documents why that verification could not run.

**Rationale:** this project already treats architecture, domain behavior,
persistence, and HTTP flows as first-class test surfaces; every new bounded
context inherits the same gate.

**Enforcement:** CI `mvn -B package --file pom.xml` gate; pull-request
review explicitly checks that the touched boundary has a failing-then-passing
test.

## Engineering Standards

- The repository standard runtime is Java 25, Maven 3.9+, and Spring Boot 4.0.x in a
  multi-module build rooted at `pom.xml`.
- New bounded-context code MUST be added to an existing module or a deliberately
  planned new module; cross-cutting technical concerns belong in `seedwork`, not
  copied into bounded contexts.
- REST adapters MUST depend on application handlers and mappers; infrastructure
  code MUST NOT call domain behavior directly except for constructing or passing
  domain types that are already part of an application contract.
- Database changes MUST include forward-only Flyway migrations and corresponding
  persistence tests that boot the Spring context.
- Generated sources under `target/generated-sources` are outputs, not hand-edited
  inputs. Their source OpenAPI documents in `src/main/resources` are the
  authoritative contract.

### Naming Reference

The following tables are the authoritative source for the naming rules that
Principle VII references. They are organized by layer. Every row is a MUST
(non-compliance is grounds for review rejection).

**Domain layer — entity accessor methods**: Use simple noun form with no `get`
prefix. Boolean state predicates use `is<State>()`. Private state-mutation
helpers use `markAs<State>()`.

- Accessors: `showId()`, `bookingId()`, `status()`
- Predicates: `isInitiated()`, `isConfirmed()`, `isCancelled()`
- Commands: imperative verb — `confirm()`, `cancel()`, `reserve()`
- Private helpers: `markAsConfirmed()`, `markAsCancelled()`

**Domain layer — repository interfaces**: Follow the Spring Data naming
contract but only for the three required operations. The id-generation method
MUST be named `next<AggregateId>()`.

- `findBy<Field>(<FieldType> field)` returning `Optional<Aggregate>`
- `save(<Aggregate> aggregate)` returning `void`
- `next<AggregateId>()` returning the aggregate's identity type

**Domain layer — domain service methods**: Use `get<Type>(<IdType>)` where
`<Type>` is the domain type returned by the adapter. `<Type>` is most
commonly a **value object** that projects data from a foreign bounded
context into the owning bounded context (Anti-Corruption Layer pattern —
e.g., `HallService.getHall(HallId hallId)` returns a `Hall` value object
assembled from an external source). `<Type>` MAY be an **aggregate root**
owned by the same bounded context when a domain service genuinely needs to
hand out an aggregate (e.g., to express a domain policy that spans multiple
aggregates); this case MUST remain rare and MUST be justified in review.
Returning an entity that is not a value object and not an aggregate root
(e.g., an aggregate-owned child entity such as `Seat`) MUST NOT be done —
child entities are reachable only through their owning aggregate.

**Application layer — command handler classes**: `<Domain>CommandHandler`
(interface) and `<Domain>CommandHandlerImpl` (implementation). Write
interfaces carry `@Transactional`; read interfaces carry
`@Transactional(readOnly = true)`.

**Application layer — command handler methods**: Use `<verb><Noun>(<Command>)`
where verb is a domain imperative (e.g., `initiateBooking`, `confirmBooking`,
`cancelBooking`, `reserveSeat`, `releaseSeat`). The corresponding command type
is `<VerbNoun>Command` and result type (when non-void) is `<VerbNoun>Result`.

**Application layer — query handler classes**: `<Domain>QueryHandler`
(interface) and `Jpa<Domain>QueryHandler` (implementation). Interfaces carry
`@Transactional(readOnly = true)`.

**Application layer — query handler methods**: Singleton lookups MUST use
`get<Entity>(<Query>)` returning a `<Noun>DetailView`; collection lookups MUST
use `list<Entities>(<Query>)` returning `List<<Noun>SummaryView>`; filtered or
sorted lookups MUST use `search<Entities>(<Query>)` returning
`List<<Noun>SummaryView>`. Query types follow `Get<Entity>Query`,
`List<Entities>Query`, and `Search<Entities>Query` respectively.

**Application layer — event handler classes**: `<Domain>EventHandler` annotated
`@Service @Transactional` at class level. Event listener methods MUST be named
`on<EventType>(EventType event)` and annotated `@EventListener`
(e.g., `onBookingConfirmed(BookingConfirmed event)`).

**Infrastructure layer — mapper classes**: Annotated `@Component` (not
`@Service`). Public methods use `to<ResponseType>(<ViewType>)` for all mapping
operations. Private helper methods for nested collections follow the same
`to<NestedType>(list)` pattern. No `from`, `map`, or `convert` prefixes.

**Test methods**: MUST follow `<methodUnderTest>With<StateOrInput>Should<ExpectedBehavior>()`.
The `With<StateOrInput>` segment is omitted only when the state is the single
obvious happy-path (e.g., `saveShouldSaveBooking()`). Test bodies MUST use
`// Arrange`, `// Act`, `// Assert` inline comments to delimit sections.

- Happy path: `initiateBookingShouldInitiateBookingAndReturnBookingId()`
- Alternate state: `confirmWithConfirmedBookingShouldDoNothingAndRaiseNoEvent()`
- Error path: `confirmWithCancelledBookingShouldThrowBookingException()`

**Test fixture classes**: Each aggregate MUST have a companion `<Aggregate>Fixture`
class in the test source tree. Fixture classes MUST expose `public static`
factory methods for every named pre-built state (e.g.,
`BookingFixture.newInitiatedBooking(showId, bookingId)`,
`BookingFixture.newConfirmedBooking(showId, bookingId)`). Factory methods MUST
clear raised events via `dispatchEvents(Consumers.noop())` so that test
assertions start from a clean event slate.

**Domain-owned child entities** (entities exclusively owned by an aggregate
root, e.g., `Seat` inside `Show`): their primary constructor MUST be
package-private so that only aggregate-root methods within the same package
can instantiate them. A `protected` no-arg constructor for JPA MUST also be
provided.

### Aggregate Class Layout

Every aggregate and entity class MUST follow this top-to-bottom ordering:

1. Domain fields (the aggregate's meaningful state)
2. Public constructor(s) and factory methods (domain creation logic, including
   `raiseEvent()` for creation events)
3. Public accessor and command methods (domain behavior)
4. Private helpers (`markAs<State>()`, `seat()`, etc.)
5. JPA infrastructure: `private Long id` surrogate key field, then `protected`
   no-arg constructor
6. `equals()` and `hashCode()` overrides

This ordering keeps domain logic visually prominent and makes JPA persistence
concerns clearly subordinate. The structural rule that the `private Long id`
field is placed near the bottom complements Principle IV, which governs the
identity semantics (domain-identity-based `equals()`/`hashCode()`; surrogate
key excluded).

### RESTful API Design

#### OpenAPI-First Contract

The OpenAPI YAML document in `src/main/resources/static/` is the sole
authoritative HTTP contract for each bounded context. Controllers MUST NOT be
written before the YAML exists. The `openapi-generator-maven-plugin` generates
`<Domain>Operations` interfaces with `interfaceOnly=true`, `useTags=true`, and
`apiNameSuffix=Operations`; these generated interfaces MUST NOT be edited by
hand. REST controllers MUST be annotated `@RestController`, MUST implement the
generated `<Domain>Operations` interface, and MUST inject command handler,
query handler, and mapper exclusively via constructor injection.

#### HTTP Method Semantics

HTTP methods MUST map to domain intent as follows:

- `POST` — resource creation (initiates a new aggregate). The request MUST
  target a **collection resource URI** (e.g., `POST /shows/{show_id}/bookings`,
  `POST /shows`), never an individual resource or action endpoint. Returns
  `201 Created` with a minimal body containing the new resource identifier.
- `PUT` — idempotent state transition (moves a resource to a named target
  state). Returns `204 No Content`. No request body; the target state is
  encoded in the URL path segment (e.g., `PUT /confirmed-bookings/{booking_id}`).
  Calling the same `PUT` on an already-transitioned resource MUST be safe and
  return `204`.
- `DELETE` — cancellation or removal of a resource or sub-resource. Returns
  `204 No Content`.
- `GET` — read-only query. Returns `200 OK` with the resource or collection body.

`PATCH` MUST NOT be used. Partial updates are expressed as explicit domain
state-transition `PUT` operations.

#### URL Path Conventions

- Resource path segments MUST use **plural English nouns** in **kebab-case**
  (e.g., `/bookings`, `/shows`, `/reserved-seats`, `/confirmed-bookings`).
- Path parameters MUST use **snake_case** (e.g., `{booking_id}`, `{show_id}`,
  `{seat_number}`).
- Ownership relationships MUST be expressed as nested paths:
  `/{parent-resource}/{parent_id}/{child-resource}` (e.g.,
  `/shows/{show_id}/bookings`, `/shows/{show_id}/seats`,
  `/bookings/{booking_id}/reserved-seats/{seat_number}`).
- Idempotent state transitions whose target state names a specific subset of
  the resource collection MUST use a **state-scoped collection path** rather
  than a sub-resource action:
  `PUT /{state-noun}-{resource}/{resource_id}` (e.g.,
  `PUT /confirmed-bookings/{booking_id}`,
  `PUT /redeemed-tickets/{ticket_id}`).
- Search endpoints MUST use the path prefix `/search/{resource}` (e.g.,
  `GET /search/shows`) rather than adding filter query parameters to the plain
  list endpoint.

#### Pagination

All collection endpoints MUST support **offset-limit pagination** using exactly
these query parameters:

- `offset` — `int64`, minimum `0`, default `0`. The number of records to skip.
- `limit` — `int32`, minimum `1`, maximum `100`, default `10`. The page size.

Page-based (`page`/`size`) pagination MUST NOT be used.

#### Search

Search endpoints accept a `q` query parameter (`string`, `minLength: 1`,
`maxLength: 20`, required) alongside the standard `offset`/`limit` parameters.
The `q` value is passed to the application layer as a filter pattern.

#### HTTP Response Status Codes

| Scenario                                                                                     | Status                      |
|----------------------------------------------------------------------------------------------|-----------------------------|
| Successful resource creation (`POST`)                                                        | `201 Created`               |
| Successful state transition (`PUT`) or removal (`DELETE`)                                    | `204 No Content`            |
| Successful retrieval (`GET`)                                                                 | `200 OK`                    |
| Invalid parameters or malformed request                                                      | `400 Bad Request`           |
| Resource not found                                                                           | `404 Not Found`             |
| HTTP method not supported on the path                                                        | `405 Method Not Allowed`    |
| Acceptable content type unavailable                                                          | `406 Not Acceptable`        |
| Domain invariant violation, optimistic-locking conflict, or duplicate constraint violation   | `409 Conflict`              |
| Domain precondition violation                                                                | `422 Unprocessable Content` |
| Unhandled server error                                                                       | `500 Internal Server Error` |

#### Response Body Structure

- **Single-resource responses** (`GET /{resource}/{id}`): flat object with all
  resource fields; no outer wrapper.
- **Collection responses** (`GET /{resource}`, `GET /search/{resource}`):
  wrapper object with a single top-level array property named after the
  resource plural (e.g., `{ "bookings": [...] }`, `{ "shows": [...] }`).
- **Creation responses** (`POST`): minimal flat object containing only the
  generated identifier (e.g., `{ "bookingId": "B0..." }`).
- **No-content responses** (`PUT` state transitions, `DELETE`): empty body.

#### Error Response Format (RFC 7807)

All error responses MUST use content-type `application/problem+json` and
conform to RFC 7807 Problem Details with these fields:

- `type` (URI, required) — problem type identifier
- `title` (string, required) — human-readable summary
- `status` (integer, required) — HTTP status code
- `detail` (string, optional) — contextual explanation
- `instance` (URI, required) — the request URI that triggered the error

The global `ProblemDetailExceptionHandler` `@RestControllerAdvice` in
`seedwork` converts all exceptions to Problem Detail responses. Controller-
local exception handling MUST NOT be added. Endpoints MUST declare
`produces: ["application/json", "application/problem+json"]` so that error
bodies are always delivered in the requested format.

### JPA Mapping Rules

- ORM mapping MUST be declared in XML files under `META-INF/domain/` (e.g.,
  `<aggregate>.orm.xml`). Domain classes MUST carry no JPA annotations;
  persistence metadata belongs exclusively in the ORM XML files.
- Field-level access (`<access>FIELD</access>`) MUST be used; property-level
  access MUST NOT be used.
- Aggregate identity MUST use sequence-based generation
  (`<generated-value strategy="SEQUENCE"/>`). `AUTO`, `IDENTITY`, and
  application-assigned primary keys MUST NOT be used unless an existing
  aggregate already establishes a different pattern.
- All aggregates MUST extend `AggregateRoot`, which provides the `version`
  column (optimistic locking) and the `raisedEvents` transient collection
  (mapped as `<transient>`). These MUST NOT be added directly on subclasses.
- Aggregate persistence MUST go through `saveAndPublishEvents(aggregateId,
  aggregate)` provided by `JpaAggregateRootSupport`. Direct calls to
  `CrudRepository.save()` that bypass event publication MUST NOT be made.
- Value objects held as a single attribute inside an aggregate MUST be mapped
  as `<embedded>` with `<attribute-override>` entries. Collections of
  aggregate-owned domain types — whether exclusively-owned entities (e.g.,
  `Seat` inside `Show`) or value objects — MUST be mapped as
  `<element-collection>` with the owned type declared as `<embeddable>`;
  `@OneToMany` / `@ManyToOne` associations between aggregates MUST NOT be
  used — aggregates reference each other by embedded identity only.
- Element-collections use JPA's default fetch behavior and MUST only be
  accessed within an active transaction. `open-in-view` MUST remain disabled
  (`spring.jpa.open-in-view=false`) to prevent lazy loading outside
  transaction boundaries.
- Read queries MUST be named native queries referenced via
  `@NativeQuery(name = "ViewType.methodName")` and declared in ORM XML.
  Derived query methods and inline JPQL strings MUST NOT be used.
- Query projection view types MUST be declared in separate ORM XML files
  under `META-INF/query/` (e.g., `META-INF/query/booking.orm.xml`) rather
  than mixed into the domain ORM files in `META-INF/domain/`.
- All ORM mapping resources MUST be explicitly enumerated in
  `spring.jpa.mapping-resources`; classpath scanning for ORM descriptors
  MUST NOT be used.
- `spring.jpa.hibernate.ddl-auto` MUST be set to `none`; all schema evolution
  is managed exclusively by Flyway migrations.
- String-formatted aggregate IDs MUST be generated using
  `Randoms.nextLong()` formatted as `"<PREFIX>0%016X"` where the prefix is
  a single uppercase letter identifying the aggregate type (e.g., `B0` for
  Booking, `T0` for Ticket).
- `@Transactional` MUST be declared on the application-layer interface, not
  on the implementation class. Write interfaces carry `@Transactional`; read
  interfaces carry `@Transactional(readOnly = true)`. Event handler classes
  are an exception: because they expose no separate interface, `@Transactional`
  MUST be placed on the concrete class.
- JPA repository interfaces MUST extend `Repository<Aggregate, Long>` (the
  minimal Spring Data marker) and the corresponding domain repository
  interface. They MUST also extend `JpaAggregateRootSupport` to gain event-
  publishing support. `JpaRepository` and `CrudRepository` extension MUST
  NOT be used.

### Event Publication and Delivery

The normative rules governing event publication are stated in Principle III
and are not restated here. This section fixes the **pipeline order** and
records **non-overlapping delta rules** that do not appear in Principle III.

Pipeline (fixed order, non-negotiable):

1. Aggregate domain method calls `raiseEvent(new <Event>(...))` on
   `AggregateRoot`.
2. Application-layer caller invokes `repository.save(aggregate)` on the
   domain repository; the JPA implementation routes through
   `JpaAggregateRootSupport.saveAndPublishEvents`, which flushes state and
   hands each raised event to `OutboxEventPublisher`.
3. `OutboxEventPublisher` enqueues the event as a `Message` row in the
   outbox table within the same transaction.
4. `OutboxPoller` (configured by `OutboxAutoConfiguration` in seedwork)
   drains locked batches on its trigger interval and dispatches each message
   body to `ApplicationEventPublisher.publishEvent(...)`. Successful
   dispatches are marked dequeued; failures remain with their attempt count
   incremented.
5. In-process reactions are implemented as `@EventListener` methods on
   application-layer `<Domain>EventHandler` classes. Out-of-process
   integration is implemented by an additional in-process outbox consumer
   (`@EventListener`) in the owning bounded context per Principle III.

Delta rules (not stated elsewhere):

- Bounded contexts MUST NOT declare their own `EventPublisher` beans, their
  own poller threads, or their own outbox tables; the seedwork outbox is
  shared infrastructure.
- The `Message.groupId` used by `OutboxEventPublisher.publishEvent(groupId,
  event)` MUST be the stringified aggregate identity so that ordering
  guarantees (per-group FIFO) align with aggregate transactional boundaries.
- Flyway migrations for the outbox schema (seedwork `V0_0__outbox.sql`) MUST
  NOT be altered by bounded-context migrations; bounded contexts only consume
  the outbox API, they do not extend its schema.
- Tests that assert on event publication MUST use aggregate
  `.dispatchEvents()` in unit tests (see `BookingFixture.newInitiatedBooking`)
  and the real outbox path in `PersistenceTest` integration tests; mocking
  `ApplicationEventPublisher` or `OutboxEventPublisher` to shortcut the
  pipeline MUST NOT be done.

### CQRS and Application-Layer Orchestration

The application layer MUST maintain strict separation between command (write)
and query (read) paths.

**Command path**: Command handler interfaces (`<Domain>CommandHandler`) and
their `@Service` implementations (`<Domain>CommandHandlerImpl`) accept single
command records and follow the load-invoke-save pattern: load aggregate(s)
from repositories, invoke domain methods, and save through repositories.
Command handlers MUST NOT contain business logic; they orchestrate domain
operations only.

**Query path**: Query handler interfaces (`<Domain>QueryHandler`) and their
infrastructure implementations (`Jpa<Domain>QueryHandler`) return view
projection records (`<Noun>DetailView`, `<Noun>SummaryView`). Query handlers
MUST NOT load full aggregates for read operations; they MUST use dedicated
named native queries that return projection types directly.

**Event handler path**: Event handler classes (`<Domain>EventHandler`) MUST
live in the application layer alongside command and query handlers. They
follow the load-invoke-save pattern defined in Principle II and MUST be
idempotent per Principle III (outbox delivery is at-least-once). Handlers
MUST NOT assume events arrive exactly once or in any particular order across
aggregates.

**Application package structure**: Each aggregate's application package MUST
be organized as:

- `application/<aggregate>/` — handler interfaces, implementations, and event
  handlers
- `application/<aggregate>/command/` — command records and result records
- `application/<aggregate>/query/` — query records and view projection records

**Infrastructure package structure**: Infrastructure implementations
supporting CQRS MUST be organized as:

- `infrastructure/persistence/<aggregate>/` — JPA repository and query handler
  implementations
- `infrastructure/web/<aggregate>/` — REST controller and response mapper
- `infrastructure/service/<service>/` — domain service adapter implementations

### Test Infrastructure Conventions

Persistence integration tests MUST extend the shared `PersistenceTest` base
class (from seedwork) and use `@SpringBootTest(webEnvironment = NONE)`.
Persistence tests MUST perform writes and reads in separate transactions to
verify actual persistence rather than first-level cache hits.

Controller integration tests MUST extend the shared `ControllerTest` base
class (from seedwork) and use `@SpringBootTest(webEnvironment = MOCK)`. Tests
MUST obtain a `MockMvc` instance from the base class and use it for
request/response assertions. Handler dependencies in controller tests MUST be
replaced with `@MockitoBean` to isolate the web layer from application logic.

Application-layer unit tests MUST use `@ExtendWith(MockitoExtension.class)`
with mocked repository dependencies. Repository interactions MUST be verified
through argument captures or mock verifications to confirm correct aggregate
state changes and save calls.

### Clock and Time Access

All production code that requires the current time MUST obtain it from the
auto-configured `Clock` bean provided by `ClockAutoConfiguration` in seedwork
(which returns `Clock.systemUTC()`). Direct calls to `Instant.now()`,
`LocalDateTime.now()`, `Clock.systemUTC()`, or any other static time-retrieval
method MUST NOT be made in application and infrastructure code. The `Clock`
instance MUST be injected via constructor injection, following the same
dependency-injection discipline as repositories and other collaborators.

In tests, the `Clock` MUST always be fixed (e.g.,
`Clock.fixed(instant, ZoneOffset.UTC)`) so that time-dependent behavior is
deterministic and repeatable. Unit tests MUST construct or mock a fixed
`Clock` directly; integration tests that boot the Spring context MUST
override the auto-configured `Clock` bean with a fixed instance via a test
configuration class.

**Rationale:** the seedwork auto-configuration already centralizes clock
access for the outbox infrastructure; extending this rule to all bounded
contexts prevents non-deterministic test failures and makes time-sensitive
logic explicitly testable.

## Delivery Workflow

- Plans MUST identify affected modules, affected layers, required tests, and
  API or migration changes before implementation starts.
- Specifications MUST define acceptance scenarios, architecture constraints,
  testing coverage, and measurable success criteria that reflect this
  constitution.
- Task lists MUST include test work, implementation work, and any contract or
  migration updates required for each user story; test tasks are mandatory,
  not optional.
- Code review and compliance review MUST reject work that weakens architecture
  rules, skips required automated coverage, edits generated artifacts
  directly, or omits migration/contract updates that the change depends on.

## Governance

This constitution supersedes conflicting local habits and template defaults,
and binds every bounded context in the repository — current and future.
Amendments MUST be made in the same change set as any template or guidance
updates required to keep planning, specification, and task-generation
artifacts consistent. Compliance review MUST happen in every plan, task list,
pull request, and release candidate review, with explicit confirmation that
architecture rules, required automated tests, and OpenAPI and Flyway assets
are still satisfied.

### Agent Obligations

These clauses apply to every coding agent (including Claude Code) that
operates on this repository:

- **Re-read before planning and implementation.** The agent MUST re-read this
  constitution at the start of every `/speckit.plan`, `/speckit.tasks`, and
  `/speckit.implement` invocation. When justifying a design choice in
  generated artifacts, the agent MUST cite the relevant principle number(s)
  (e.g., "per Principle III").
- **Surface conflicts before acting.** When a user request, inherited task, or
  external tool output appears to conflict with a principle, the agent MUST
  surface the conflict — citing the specific principle number and the
  apparent conflict — and obtain explicit direction before implementing. The
  agent MUST NOT silently deviate from a principle, and MUST NOT silently
  comply with a request that would violate one.
- **Amendment path for principled disagreement.** If the agent or the user
  believes a principle is wrong for the current context, the correct remedy
  is an explicit constitution amendment (MAJOR/MINOR/PATCH per the versioning
  policy below), not a one-off exception.

### Versioning Policy

Versioning follows semantic versioning for governance: MAJOR for backward-
incompatible principle removals, redefinitions, or renumbering; MINOR for new
principles, materially expanded obligations, or reorganizations that change
the structure of the document without weakening rules; and PATCH for
clarifications that do not change enforcement (wording, typo fixes, uniform
vocabulary adjustments, non-normative refinements). Ratification records the
original adoption date of this document; `Last Amended` MUST be updated
whenever the constitution changes.

**Version**: 4.1.1 | **Ratified**: 2026-03-15 | **Last Amended**: 2026-04-18