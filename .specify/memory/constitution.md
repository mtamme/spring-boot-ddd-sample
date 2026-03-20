<!--
Sync Impact Report
- Version change: 1.1.0 -> 1.2.0
- Modified principles:
  - Principle II: expanded with idempotent state-transition rule
  - Principle VII: expanded with event handler class/method naming,
      view projection type naming (DetailView/SummaryView),
      search query method/type naming, and test fixture class naming
- Added sections:
  - None (all changes are expansions of existing principles)
- Removed sections:
  - None
- Templates requiring updates:
  - ✅ updated .specify/templates/plan-template.md
      (Constitution Check item VII extended with event handler, view,
       and search-query naming; idempotency note added to item II)
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
stereotypes already enforced by architecture tests. Rationale: the repository is
structured around DDD modules and ArchUnit rules; architectural drift would break
the sample's primary teaching goal.

### II. Domain Behavior Is Encapsulated

Business rules MUST be implemented in entities, value objects, domain services,
and domain events rather than in controllers, repositories, or transport DTOs.
Entities, events, and value objects MUST be modeled as separate concepts; events
and value objects MUST be immutable; entity state MUST NOT be exposed through
JavaBean getters or setters. Preconditions and invariants MUST be expressed
through domain contracts and problem types so invalid states fail fast and
consistently.

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
represented by a `public static final Problem` constant on the exception class.
Instances MUST be created only through `public static` factory methods (e.g.,
`BookingException.notFound()`) — constructors MUST be private. Exception factory
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

**Infrastructure layer — mapper methods**: Use `to<TargetType>()` for all
mapping methods regardless of source type. No `from`, `map`, or `convert`
prefixes.

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

Rationale: consistent naming lets readers locate tests by method name and
predict behavior before reading the body; the three-segment pattern encodes
exactly the information needed to triage a failure; fixture classes eliminate
duplicated state-setup boilerplate and keep test arrange-sections minimal.

## Engineering Standards

- The repository standard runtime is Java 25, Maven 3.9+, and Spring Boot 4 in a
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
- Value objects embedded in an aggregate MUST be mapped as `<embedded>` with
  `<attribute-override>` entries. Collections of value objects MUST be mapped as
  `<element-collection>`; `@OneToMany` / `@ManyToOne` relationships between
  aggregates are forbidden — aggregates reference each other by embedded identity
  only.
- `FetchType.LAZY` is forbidden. All aggregate contents load together with the
  root. `open-in-view` MUST remain disabled (`spring.jpa.open-in-view=false`).
- Read queries MUST be named native queries referenced via `@NativeQuery(name =
  "ViewType.methodName")` and declared in ORM XML. Derived query methods and
  inline JPQL strings are forbidden.
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

**Version**: 1.2.0 | **Ratified**: 2026-03-15 | **Last Amended**: 2026-03-20
