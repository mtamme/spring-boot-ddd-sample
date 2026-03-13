# Spring Boot DDD Sample Constitution

Status: Ratified
Version: 1.0.0
Ratified: 2026-03-13
Last Updated: 2026-03-13

## Purpose

This constitution defines non-negotiable engineering rules for this repository.
All specifications, plans, tasks, and code changes MUST comply with these articles.

## Article I: Domain-Centric Architecture

1. The `booking` module MUST preserve the layered dependency flow:
   `domain <- application <- infrastructure`.
2. The `seedwork` module MUST preserve the layered dependency flow:
   `core <- domain <- infrastructure`.
3. Dependencies MUST point inward only; no layer may depend on outer layers.
4. Infrastructure code MUST NOT bypass application use cases to call domain behavior directly, except:
   aggregate/entity/value-object access that is part of state persistence and reconstruction.
5. Architecture invariants MUST be enforced by ArchUnit tests and MUST pass in CI.

Rationale: domain behavior remains stable, testable, and independent from transport and technical concerns.

## Article II: Domain Model Integrity

1. Domain model classes MUST reside under `*.domain..`.
2. Domain classes MUST represent domain concepts only:
   entities, events, value objects, domain services, domain repositories, and domain problem exceptions.
3. Entities, value objects, and events MUST be modeled separately; one type MUST NOT implement another.
4. Value objects and events MUST be immutable.
5. Domain preconditions and invariants MUST be guarded with `Contract.require(...)` and `Contract.check(...)`.
6. JavaBean-style mutation APIs are forbidden in domain objects:
   methods matching `get[A-Z].*` and `set[A-Z].*` MUST NOT be introduced.

Rationale: model clarity and invariant safety are higher priority than convenience APIs.

## Article III: Application Layer Semantics

1. Application services MUST orchestrate use cases and domain collaboration, not contain infrastructure concerns.
2. Command/query payloads SHOULD be modeled as Java `record` types.
3. Command handlers SHOULD be named `*CommandHandler` (interface) and `*CommandHandlerImpl` (implementation).
4. Query handlers SHOULD be named `*QueryHandler` with a single infrastructure-backed implementation.
5. Domain event reactions MUST be explicit and idempotent where behavior can be retried.

Rationale: use case orchestration remains explicit and stable over infrastructure evolution.

## Article IV: Infrastructure Adapter Rules

1. Web controllers MUST implement generated OpenAPI interfaces (`*Operations`) and remain thin adapters.
2. Web mappers (`*Mapper`) MUST perform representation mapping only.
3. Persistence adapters MUST use repository interfaces in the domain and provide infrastructure implementations.
4. Read-side SQL queries MUST be declared as named native queries and mapped to projection/view records.
5. Database changes MUST be delivered via Flyway migrations using `V<major>_<minor>__<name>.sql` naming.

Rationale: adapters stay replaceable and behavior remains centered on domain/application code.

## Article V: Eventing and Outbox Reliability

1. Aggregate roots MUST raise domain events through `raiseEvent(...)`.
2. Persistence save operations for aggregates MUST call `saveAndPublishEvents(...)`.
3. Published event subject names MUST match event class simple names.
4. Outbox processing MUST preserve per-group ordering semantics (`groupId + sequenceNumber`).
5. Outbox polling and locking behavior MUST be configurable through `outbox.*` properties.

Rationale: reliable asynchronous delivery is part of core platform behavior, not optional integration glue.

## Article VI: API and Error Contract

1. Public HTTP APIs MUST be contract-first from OpenAPI documents in `src/main/resources/static`.
2. Generated API interfaces/models MUST remain authoritative for controller method signatures.
3. Validation and request failures MUST map to RFC 7807 Problem Details responses.
4. Domain failures MUST surface through typed `ProblemException` instances with stable `urn:problem:*` types.
5. Pagination conventions MUST use `offset` and `limit` with defaults consistent with OpenAPI.

Rationale: consumers rely on stable contracts, predictable status codes, and machine-readable errors.

## Article VII: Naming and Structural Conventions

1. Package naming:
   `com.github.<module>.<layer>.<subdomain_or_adapter>`.
2. Type naming:
   `*Id` for identifiers, `*Exception` for domain/application errors, `*Event` or past-tense event names.
3. Identifier formats MUST remain stable:
   - `BookingId`: `B0[0-9A-F]{16}`
   - `ShowId`: `S0[0-9A-F]{16}`
   - `MovieId`: `M0[0-9A-F]{16}`
   - `HallId`: `H0[0-9A-F]{16}`
   - `TicketId`: `T0[0-9A-F]{16}`
4. SQL/DDL naming SHOULD use snake_case with suffix conventions (`_pk`, `_fk`, `_ui`, `_i`).
5. Domain accessors SHOULD use ubiquitous language methods (`bookingId()`, `showId()`) instead of bean getters.

Rationale: consistent naming lowers cognitive load and protects integration compatibility.

## Article VIII: Testing and Delivery Gates

1. `mvn clean package` MUST pass before merge.
2. CI on pull requests MUST execute the same Maven packaging gate.
3. New behavior MUST include tests at the relevant layer:
   domain unit tests, adapter tests, and architecture tests where applicable.
4. Integration/persistence tests that mutate schema state MUST clean with Flyway after each test.
5. Test classes MUST be named with the `*Test` suffix. Behavioral `@Test` methods SHOULD follow the existing
   lowerCamelCase `...Should...` naming style, while `@ArchTest` methods MAY use explicit invariant-style names.
6. Tests SHOULD follow explicit Arrange/Act/Assert sections and use existing fixture helpers (`*Fixture`)
   where available.
7. Web adapter tests SHOULD extend `ControllerTest` and persistence integration tests SHOULD extend
   `PersistenceTest` to preserve consistent Spring Boot test setup.
8. Any change violating this constitution MUST either:
   - be redesigned to comply, or
   - include an approved constitution amendment.

Rationale: fast feedback and architectural guardrails prevent silent degradation.

## Governance

1. This constitution supersedes conflicting local practices or undocumented conventions.
2. Amendments require:
   - explicit change proposal,
   - updated rationale,
   - version bump under semantic policy:
     - MAJOR: removes or redefines an article in a breaking way,
     - MINOR: adds a new article or materially expands required behavior,
     - PATCH: clarifications without changing normative meaning.
3. Every feature plan at `.specify/specs/<feature>/plan.md` MUST include a constitution compliance check against all articles.
4. Reviewers MUST block merges that violate mandatory (`MUST`) statements.
