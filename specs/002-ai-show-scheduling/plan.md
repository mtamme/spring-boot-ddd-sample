# Implementation Plan: Move Show Scheduling Policy to Domain

**Branch**: `002-ai-show-scheduling` | **Date**: 2026-03-27 | **Spec**: [spec.md](spec.md)
**Input**: Move the count query from `JpaShowSchedulingPolicy` into `ShowRepository` using ORM XML. Implement
`ShowSchedulingPolicy` as a domain `@Service` using the new repository query method.

## Summary

Refactor the show overlap check from an infrastructure-layer JPA service (`JpaShowSchedulingPolicy`) into the domain
layer. The SQL count query moves into `ShowRepository` (declared in `show.orm.xml`, implemented via `@NativeQuery` in
`JpaShowRepository`). `ShowSchedulingPolicy` becomes a `@Service`-annotated domain service that delegates to the
repository method. The infrastructure class `JpaShowSchedulingPolicy` is deleted.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0.4, Spring Data JPA, H2 (local profile)
**Storage**: H2 via JPA with ORM XML mappings, Flyway migrations
**Testing**: JUnit 5, Mockito, Spring Boot Test (`PersistenceTest` base class)
**Target Platform**: JVM (local dev)
**Project Type**: Web service (DDD sample)
**Performance Goals**: p95 < 200ms for scheduling command
**Constraints**: No migration changes needed (schema unchanged); domain layer must remain technology-agnostic
**Scale/Scope**: Single bounded context refactoring, no API changes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Module and layer impact is explicit: `scheduling` domain gains a `@Service` domain service and an additional
  repository method; `scheduling` infrastructure loses `JpaShowSchedulingPolicy` and gains a `@NativeQuery` method +
  named native query in ORM XML. Domain layer contains only permitted types (Entity, Event, ValueObject, @Service domain
  service, ProblemException, interface).
- [x] Domain changes keep business rules in domain types; `ShowSchedulingPolicy` moves business logic (overlap check)
  from infrastructure to domain `@Service`; no aggregate changes needed.
- [x] Required automated tests identified: unit test for `ShowSchedulingPolicy` domain service; persistence integration
  test for `countOverlappingShows` named native query; existing `ShowCommandHandlerImplTest` updated (mock target
  changes from interface to concrete class — still works with Mockito).
- [x] ORM XML updated: named native query added to `META-INF/domain/show.orm.xml`; `spring.jpa.mapping-resources`
  already includes this file — no config change needed. No Flyway migration needed (schema unchanged). No API changes.
- [x] Performance budget: COUNT query with existing index on `hall_id` is O(shows-per-hall), bounded; no N+1 risk;
  single query per scheduling command.
- [x] All precondition checks use `Contract.require()`; `ShowException.overlap()` uses `Problem.conflict()` (HTTP 409) —
  unchanged.
- [x] Naming follows conventions: `countOverlappingShows` in repository; `ensureNoOverlap` retained in domain service;
  test names follow `<method>With<State>Should<Behavior>()`.
- [x] RESTful API design: N/A — no API changes in this refactoring.
- [x] Cross-aggregate side effects: N/A — no cross-aggregate changes.
- [x] CQRS separation maintained: the count query is a domain repository method (not a query handler projection); it
  returns a scalar `long`, not a view type. Domain ORM XML is the correct location since this serves entity/aggregate
  operations, not CQRS read projections.
- [x] Test infrastructure: new persistence test extends `PersistenceTest` with separate read/write transactions; domain
  service unit test uses `@ExtendWith(MockitoExtension.class)` with mocked repository.
- [x] Clock access: N/A — no time access changes in this refactoring.

## Project Structure

### Documentation (this feature)

```text
specs/002-ai-show-scheduling/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
└── quickstart.md        # Phase 1 output
```

### Source Code (affected files)

```text
scheduling/
├── src/main/java/com/github/scheduling/
│   ├── domain/show/
│   │   ├── ShowRepository.java           # ADD: countOverlappingShows method
│   │   └── ShowSchedulingPolicy.java     # CHANGE: interface → @Service class
│   └── infrastructure/persistence/show/
│       ├── JpaShowRepository.java        # ADD: @NativeQuery + default bridge
│       └── JpaShowSchedulingPolicy.java  # DELETE
├── src/main/resources/
│   └── META-INF/domain/show.orm.xml      # ADD: named native query
└── src/test/java/com/github/scheduling/
    ├── domain/show/
    │   └── ShowSchedulingPolicyTest.java  # NEW: domain service unit test
    ├── application/show/
    │   └── ShowCommandHandlerImplTest.java # UPDATE: mock concrete class
    └── infrastructure/persistence/show/
        └── JpaShowRepositoryTest.java     # ADD: count query integration test
```

**Structure Decision**: Changes span domain and infrastructure layers of the `scheduling` module only. No new modules,
no new ORM XML files, no migration changes. The named native query is added to the existing domain ORM XML (
`META-INF/domain/show.orm.xml`) because it serves domain repository operations, not CQRS query handler projections.

## Complexity Tracking

No constitution violations.

---

## Phase 0: Research

No unknowns to resolve. All technologies and patterns are already established in the codebase:

- Named native queries in ORM XML: established in `META-INF/query/show.orm.xml` and `seedwork/META-INF/outbox.orm.xml`
- `@NativeQuery` annotation: established in `JpaShowRepository`
- Domain `@Service` classes: permitted by constitution (§I)
- Spring Data `Repository` with default methods: established in `JpaShowRepository`

### Decision Log

| Decision                                                                                                         | Rationale                                                                                                                                                                                                                                                                   | Alternatives Rejected                                                                                                                                                                                                   |
|------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Named native query in domain ORM XML (`META-INF/domain/show.orm.xml`)                                            | The count query serves the domain repository's `countOverlappingShows` method, not a CQRS query handler projection. Constitution separates domain ORM (`META-INF/domain/`) from query projection ORM (`META-INF/query/`).                                                   | Query ORM XML rejected: that file is for `<Noun>DetailView`/`<Noun>SummaryView` result set mappings used by query handlers. A scalar `long` count for domain logic doesn't belong there.                                |
| Bridge pattern: domain interface method with value objects → JPA default method → `@NativeQuery` with raw params | Preserves domain purity (repository interface uses `HallId`, `Instant`) while satisfying `@NativeQuery`/`@Param` requirements for raw parameter binding. Follows established `save()` default-method pattern.                                                               | Direct raw params on domain interface rejected: violates domain type purity. Single JPA method accepting value objects rejected: `@Param` binding doesn't auto-extract embedded value object fields for native queries. |
| `ShowSchedulingPolicy` as concrete `@Service` (not interface + impl)                                             | The policy has one implementation and lives entirely in the domain. No infrastructure concern remains after the query moves to the repository. An interface adds indirection without value.                                                                                 | Keeping interface + domain impl rejected: unnecessary abstraction for a single domain service with no infrastructure concern.                                                                                           |
| Method name `countOverlappingShows`                                                                              | Clearly describes what the repository returns (a count of overlapping shows). Follows domain language. The existing repository naming convention (`findBy<Field>`, `save`, `next<Id>`) covers the three standard operations; this is a justified domain-specific extension. | `existsOverlappingShow` (returns boolean) rejected: the count conveys richer information and matches the existing SQL pattern. `findOverlappingShows` rejected: returns full aggregates unnecessarily.                  |

---

## Phase 1: Design

### Data Model

No data model changes. The database schema (`show` table) and Flyway migration (`V1_0__show.sql`) remain unchanged. The
refactoring only moves where the SQL query is declared and how the result is consumed.

### Interface Changes

#### Domain Layer

**`ShowRepository`** — add one method:

```java
long countOverlappingShows(HallId hallId, Instant start, Instant end);
```

Returns the count of existing shows in the given hall whose time range `[scheduledAt, scheduledAt + runtimeMinutes)`
overlaps with the interval `[start, end)`.

**`ShowSchedulingPolicy`** — convert from interface to `@Service` class:

```java
@Service
public class ShowSchedulingPolicy {
  private final ShowRepository showRepository;

  public ShowSchedulingPolicy(ShowRepository showRepository) { ... }

  public void ensureNoOverlap(HallId hallId, Instant start, Instant end) {
    Contract.require(hallId != null);
    Contract.require(start != null);
    Contract.require(end != null);
    long count = showRepository.countOverlappingShows(hallId, start, end);
    Contract.check(count == 0, ShowException::overlap);
  }
}
```

#### Infrastructure Layer

**`JpaShowRepository`** — add bridge + `@NativeQuery`:

```java
@Override
default long countOverlappingShows(final HallId hallId, final Instant start, final Instant end) {
  return countOverlappingShowsQuery(hallId.value(), start, end);
}

@NativeQuery(name = "ShowRepository.countOverlappingShows")
long countOverlappingShowsQuery(@Param("hall_id") String hallId,
                                @Param("start_time") Instant start,
                                @Param("end_time") Instant end);
```

**`META-INF/domain/show.orm.xml`** — add named native query:

```xml
<named-native-query name="ShowRepository.countOverlappingShows">
  <query>
    SELECT COUNT(*)
    FROM show s
    WHERE s.hall_id = :hall_id
      AND s.scheduled_at &lt; :end_time
      AND DATEADD(MINUTE, s.movie_runtime_minutes, s.scheduled_at) > :start_time
  </query>
</named-native-query>
```

**Delete**: `JpaShowSchedulingPolicy.java`

### Test Plan

1. **`ShowSchedulingPolicyTest`** (new, domain unit test):

- `ensureNoOverlapWithNoOverlapShouldNotThrow()` — mock `countOverlappingShows` returns 0
- `ensureNoOverlapWithOverlapShouldThrowShowException()` — mock returns > 0, assert `ShowException` thrown

2. **`JpaShowRepositoryTest`** (update, persistence integration test):

- `countOverlappingShowsWithOverlapShouldReturnCount()` — persist a show, query overlapping range, assert count > 0
- `countOverlappingShowsWithNoOverlapShouldReturnZero()` — persist a show, query non-overlapping range, assert
  count == 0

3. **`ShowCommandHandlerImplTest`** (update):

- Change `@Mock ShowSchedulingPolicy` from interface mock to concrete class mock — Mockito supports this with no code
  changes beyond removing the `ShowSchedulingPolicy` import if it was previously the interface import path.

### Quickstart

```bash
# Build and test
mvn -B package --file pom.xml

# Run scheduling context
cd scheduling && mvn spring-boot:run -Dspring-boot.run.profiles=default,local
```

---

## Artifacts Generated

| Artifact            | Path                                   | Status                 |
|---------------------|----------------------------------------|------------------------|
| Implementation Plan | `specs/002-ai-show-scheduling/plan.md` | Complete               |
| Research            | Inline (Phase 0 above)                 | Complete — no unknowns |
| Data Model          | N/A — no schema changes                | N/A                    |
| Contracts           | N/A — no API changes                   | N/A                    |
