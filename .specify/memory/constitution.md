<!--
Sync Impact Report
- Version change: 1.2.1 -> 1.3.0
- Modified principles:
  - Principle II: added event-in-constructor rule and equals/hashCode on domain identity rule
  - Principle VI: added Problem constant naming convention (<STATE>_PROBLEM) and HTTP
      status mapping (notFound -> 404, conflict -> 409)
  - Principle VII: added mapper @Component annotation rule and package-private
      constructor rule for exclusively-owned child entities
- Added sections:
  - Engineering Standards: aggregate structural layout (surrogate key at bottom,
      protected no-arg JPA constructor, equals/hashCode)
  - Engineering Standards: REST controller conventions (Operations interface, HTTP
      status codes)
  - JPA Mapping Rules: ddl-auto=none, explicit ORM resource enumeration,
      META-INF/query/ separation for projections, ID generation format
- Removed sections:
  - None
- Templates requiring updates:
  - ✅ updated .specify/templates/plan-template.md
      (Constitution Check extended: equals/hashCode, controller Operations interface,
       Problem naming, ORM enumeration)
  - ✅ no changes required in .specify/templates/spec-template.md
  - ✅ no changes required in .specify/templates/tasks-template.md
  - ✅ no commands directory present at .specify/templates/commands
  - ✅ no runtime guidance updates required in README.md
- Follow-up TODOs:
  - None
-->
# Spring Boot DDD Sample Constitution

## Core Principles

### I. Domain-Centric Boundaries

All production code MUST live inside explicit inward-facing layers. In bounded
contexts such as `booking`, dependencies MUST follow
`infrastructure -> application -> domain`; in shared modules such as `seedwork`,
dependencies MUST follow `infrastructure -> domain -> core`. Domain classes MUST
remain technology-agnostic and MUST NOT depend on web, persistence, messaging,
or framework-specific delivery concerns beyond the narrowly approved Spring
stereotypes already enforced by architecture tests.

The domain layer MUST contain only the following concept types: classes
implementing `Entity`, `Event`, or `ValueObject`; `@Service`-annotated domain
service classes; `ProblemException` subclasses; and interfaces. No other class
types are permitted in the domain layer.

Rationale: the repository is structured around DDD modules and ArchUnit rules;
architectural drift would break the sample's primary teaching goal.

### II. Domain Behavior Is Encapsulated

Business rules MUST be implemented in entities, value objects, domain services,
and domain events rather than in controllers, repositories, or transport DTOs.
Entities, events, and value objects MUST be modeled as separate concepts; events
and value objects MUST be immutable; entity state MUST NOT be exposed through
JavaBean getters or setters.

Domain events representing creation facts MUST be raised in the aggregate
constructor immediately after initial state is set (e.g., `Booking` constructor
calls `raiseEvent(new BookingInitiated(...))`). Raising creation events in
factory or service methods is forbidden.

Every entity and aggregate MUST implement `equals()` and `hashCode()` using its
domain identity value object (e.g., `bookingId`, `showId`, `ticketId`). The JPA
surrogate key (`private Long id`) MUST NOT participate in equality or hash
computation.

Preconditions and invariants MUST be expressed through domain contracts and
problem types so invalid states fail fast and consistently.

Aggregate state transitions MUST be idempotent where the target state is already
reached: after verifying via `Contract.check()` that the precondition allows the
transition, an early-return guard MUST silently exit when the aggregate is already
in the target state, so that repeat invocations are safe and raise no spurious
events. Example: `confirm()` allows INITIATED or CONFIRMED as valid preconditions
and returns immediately if already CONFIRMED.

Rationale: the current codebase uses contract checks, explicit event publication,
and rich domain models instead of anemic records; idempotent transitions prevent
duplicate events and make commands safe to retry.

### III. Tests Are Mandatory Quality Gates

Every change MUST include the narrowest automated tests that prove the behavior
and the boundary it touches. Domain behavior changes MUST add or update unit
tests in `src/test/java`; application orchestration changes MUST add service or
event-handler tests; web and persistence changes MUST add Spring Boot
integration tests; structural changes MUST keep ArchUnit tests passing. No
feature is complete until `mvn -B package --file pom.xml` passes locally or the
author documents why that verification could not run. Rationale: this project
already treats architecture, domain behavior, persistence, and HTTP flows as
first-class test surfaces.

### IV. Contracts, Migrations, and Generated Interfaces Stay Aligned

Public HTTP behavior MUST be defined through versioned OpenAPI documents under
module resources, and generated interfaces MUST be treated as derived artifacts
from those specs rather than hand-edited sources. Persistent model changes MUST
be delivered through Flyway migrations and matching ORM/query mappings; ad hoc
schema mutation or `ddl-auto` driven design is forbidden. When a change affects
events, outbox processing, or API representations, the corresponding contract,
mapping, migration, and automated tests MUST be updated in the same change.
Rationale: the repository already ships OpenAPI specs, generated interfaces, ORM
mappings, and Flyway migrations as the canonical integration contracts.

### V. Consistency and Performance Budgets Are Designed Up Front

Each feature proposal MUST declare the module boundaries it touches, the data
access path it introduces, and the performance budget it must satisfy before
implementation begins. New synchronous HTTP endpoints MUST target p95 latency of
200 ms or less for single-aggregate reads and commands on the default local
profile, and MUST justify any design that risks N+1 access patterns, cross-module
chatty orchestration, or unbounded result sets. JPA `open-in-view` MUST remain
disabled, queries MUST stay explicit, and large reads MUST provide bounded search
or list semantics. Rationale: this codebase is intentionally explicit about query
handlers, mappings, and transaction boundaries, and that discipline is what keeps
sample code understandable and production-like.

### VI. Code Contracts Enforce Invariants at Every Boundary

All precondition and invariant checks MUST use `Contract.require()` and
`Contract.check()` from `seedwork`. No `if`-then-throw guards, assertion
frameworks, or ad hoc null checks are permitted as replacements.

- `Contract.require(condition)` MUST be used at method entry to validate caller
  obligations (non-null arguments, valid parameter ranges). Violation throws
  `IllegalArgumentException`.
- `Contract.require(condition, exceptionSupplier)` MUST be used when a
  precondition failure requires a typed domain exception (e.g., not-found).
- `Contract.check(condition)` MUST be used to assert domain state invariants
  mid-method. Violation throws `IllegalStateException`.
- `Contract.check(condition, exceptionSupplier)` MUST be used when an invariant
  violation requires a typed domain exception (e.g., not-confirmable, not-
  cancelable).

Domain exceptions MUST extend `ProblemException`. Each error case MUST be
represented by a `public static final Problem` constant named
`<SCREAMING_SNAKE_CASE>_PROBLEM` (e.g., `NOT_FOUND_PROBLEM`,
`NOT_CANCELABLE_PROBLEM`). Resource-not-found errors MUST use `Problem.notFound()`
which maps to HTTP 404; business-state violations MUST use `Problem.conflict()`
which maps to HTTP 409. Instances MUST be created only through `public static`
factory methods whose names match the problem suffix in lowerCamelCase (e.g.,
`notFound()`, `notCancelable()`) — constructors MUST be private. Exception factory
methods MUST be passable as method references so they can be used directly in
`Contract.check(condition, BookingException::notCancelable)`. Rationale: uniform
contract usage makes invariant enforcement visible and grep-able; the
`ProblemException` hierarchy ensures every failure carries an HTTP-mappable
problem descriptor without leaking implementation detail.

### VII. Naming Conventions Are Uniformly Applied Across All Layers

Names MUST follow the layer-specific conventions derived from the existing
codebase. Reviewers MUST reject names that diverge from these patterns.

**Domain layer — entity accessor methods**: Use simple noun form with no `get`
prefix. Boolean state predicates use `is<State>()`. Private state-mutation
helpers use `markAs<State>()`.

- Accessors: `showId()`, `bookingId()`, `status()`
- Predicates: `isInitiated()`, `isConfirmed()`, `isCancelled()`
- Commands: imperative verb — `confirm()`, `cancel()`, `reserve()`
- Private helpers: `markAsConfirmed()`, `markAsCancelled()`

**Domain layer — repository interfaces**: Follow the Spring Data naming contract
but only for the three required operations. The id-generation method MUST be
named `next<AggregateId>()`.

- `findBy<Field>(<FieldType> field)` returning `Optional<Aggregate>`
- `save(<Aggregate> aggregate)` returning `void`
- `next<AggregateId>()` returning the aggregate's identity type

**Domain layer — domain service factory methods**: Use `<entity>From(<IdType>)`
(e.g., `hallFrom(HallId hallId)`).

**Application layer — command handler classes**: `<Domain>CommandHandler`
(interface) and `<Domain>CommandHandlerImpl` (implementation). Write interfaces
carry `@Transactional`; read interfaces carry `@Transactional(readOnly = true)`.

**Application layer — command handler methods**: Use `<verb><Noun>(<Command>)`
where verb is a domain imperative (e.g., `initiateBooking`, `confirmBooking`,
`cancelBooking`, `reserveSeat`, `releaseSeat`). The corresponding command type
is `<VerbNoun>Command` and result type (when non-void) is `<VerbNoun>Result`.

**Application layer — query handler classes**: `<Domain>QueryHandler` (interface)
and `Jpa<Domain>QueryHandler` (implementation). Interfaces carry
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
class in the test source tree. Fixture classes MUST expose `public static` factory
methods for every named pre-built state (e.g., `BookingFixture.newInitiatedBooking(showId, bookingId)`,
`BookingFixture.newConfirmedBooking(showId, bookingId)`). Factory methods MUST
clear raised events via `releaseEvents(Consumers.empty())` so that test assertions
start from a clean event slate.

**Domain-owned child entities** (entities exclusively owned by an aggregate root,
e.g., `Seat` inside `Show`): their primary constructor MUST be package-private
so that only aggregate-root methods within the same package can instantiate them.
A `protected` no-arg constructor for JPA MUST also be provided.

Rationale: consistent naming lets readers locate tests by method name and
predict behavior before reading the body; the three-segment pattern encodes
exactly the information needed to triage a failure; fixture classes eliminate
duplicated state-setup boilerplate and keep test arrange-sections minimal.

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

### Aggregate Class Layout

Every aggregate and entity class MUST follow this top-to-bottom ordering:

1. Domain fields (the aggregate's meaningful state)
2. Public constructor(s) and factory methods (domain creation logic, including
   `raiseEvent()` for creation events)
3. Public accessor and command methods (domain behavior)
4. Private helpers (`markAs<State>()`, `seat()`, etc.)
5. JPA infrastructure: `private Long id` surrogate key field, then `protected`
   no-arg constructor

This ordering keeps domain logic visually prominent and makes JPA persistence
concerns clearly subordinate. The `private Long id` field MUST be placed at the
bottom and MUST NOT participate in `equals()` or `hashCode()`. Both `equals()`
and `hashCode()` MUST be implemented using the domain identity value object only.

### REST Controller Conventions

REST controllers MUST be annotated `@RestController` and MUST implement the
OpenAPI-generated `<Domain>Operations` interface (e.g., `BookingOperations`,
`ShowOperations`). Controllers MUST inject the relevant command handler, query
handler, and mapper via constructor injection. HTTP response status codes MUST
follow: `201 CREATED` for resource creation commands, `204 NO_CONTENT` for void
domain operations, and `200 OK` for queries.

### JPA Mapping Rules

- ORM mapping MUST be declared in XML files under `META-INF/domain/` (e.g.,
  `<aggregate>.orm.xml`). Domain classes MUST carry no JPA annotations;
  persistence metadata belongs exclusively in the ORM XML files.
- Field-level access (`<access>FIELD</access>`) MUST be used; property-level
  access is forbidden.
- Aggregate identity MUST use sequence-based generation
  (`<generated-value strategy="SEQUENCE"/>`). No `AUTO`, `IDENTITY`, or
  application-assigned primary keys unless an existing aggregate already
  establishes a different pattern.
- All aggregates MUST extend `AggregateRoot`, which provides the `version`
  column (optimistic locking) and the `raisedEvents` transient collection
  (mapped as `<transient>`). These MUST NOT be added directly on subclasses.
- Aggregate persistence MUST go through `saveAndPublishEvents(aggregateId,
  aggregate)` provided by `JpaAggregateRootSupport`. Direct calls to
  `CrudRepository.save()` that bypass event publication are forbidden.
- Value objects held as a single attribute inside an aggregate MUST be mapped as
  `<embedded>` with `<attribute-override>` entries. Collections of aggregate-owned
  domain types — whether exclusively-owned entities (e.g., `Seat` inside `Show`)
  or value objects — MUST be mapped as `<element-collection>` with the owned type
  declared as `<embeddable>`; `@OneToMany` / `@ManyToOne` associations between
  aggregates are forbidden — aggregates reference each other by embedded identity
  only.
- Explicit lazy entity associations are forbidden; `@OneToMany` and `@ManyToOne`
  between aggregates are replaced by embedded-identity references (see rule above).
  Element-collections use JPA's default fetch behavior (LAZY) and MUST only be
  accessed within an active transaction. `open-in-view` MUST remain disabled
  (`spring.jpa.open-in-view=false`) to prevent lazy loading outside transaction
  boundaries.
- Read queries MUST be named native queries referenced via `@NativeQuery(name =
  "ViewType.methodName")` and declared in ORM XML. Derived query methods and
  inline JPQL strings are forbidden.
- Query projection view types MUST be declared in separate ORM XML files under
  `META-INF/query/` (e.g., `META-INF/query/booking.orm.xml`) rather than mixed
  into the domain ORM files in `META-INF/domain/`.
- All ORM mapping resources MUST be explicitly enumerated in
  `spring.jpa.mapping-resources`; classpath scanning for ORM descriptors is
  forbidden.
- `spring.jpa.hibernate.ddl-auto` MUST be set to `none`; all schema evolution
  is managed exclusively by Flyway migrations.
- String-formatted aggregate IDs MUST be generated using
  `RandomSupport.nextLong()` formatted as `"<PREFIX>0%016X"` where the prefix is
  a single uppercase letter identifying the aggregate type (e.g., `B0` for
  Booking, `T0` for Ticket).
- `@Transactional` MUST be declared on the application-layer interface, not on
  the implementation class. Write interfaces carry `@Transactional`; read
  interfaces carry `@Transactional(readOnly = true)`. Event handler classes
  carry `@Transactional` at class level.
- JPA repository interfaces MUST extend `Repository<Aggregate, Long>` (the
  minimal Spring Data marker) and the corresponding domain repository interface.
  They MUST also extend `JpaAggregateRootSupport` to gain event-publishing
  support. No `JpaRepository` or `CrudRepository` extension is permitted.

## Delivery Workflow

- Plans MUST identify affected modules, affected layers, required tests, API or
  migration changes, and performance impact before implementation starts.
- Specifications MUST define acceptance scenarios, architecture constraints,
  testing coverage, and measurable success criteria that reflect this
  constitution.
- Task lists MUST include test work, implementation work, and any contract or
  migration updates required for each user story; test tasks are mandatory, not
  optional.
- Code review and compliance review MUST reject work that weakens architecture
  rules, skips required automated coverage, edits generated artifacts directly, or
  omits migration/contract updates that the change depends on.

## Governance

This constitution supersedes conflicting local habits and template defaults.
Amendments MUST be made in the same change set as any template or guidance
updates required to keep planning, specification, and task-generation artifacts
consistent. Compliance review happens in every plan, task list, pull request, and
release candidate review, with explicit confirmation that architecture rules,
required automated tests, OpenAPI and Flyway assets, and performance budgets are
still satisfied. Versioning follows semantic versioning for governance: MAJOR for
backward-incompatible principle removals or redefinitions, MINOR for new
principles or materially expanded obligations, and PATCH for clarifications that
do not change enforcement. Ratification records the original adoption date of
this document; `Last Amended` MUST be updated whenever the constitution changes.

**Version**: 1.3.0 | **Ratified**: 2026-03-15 | **Last Amended**: 2026-03-20
