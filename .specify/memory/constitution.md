<!--
Sync Impact Report
- Version change: none -> 1.0.0
- Modified principles:
  - template placeholder -> I. Domain-Centric Boundaries
  - template placeholder -> II. Domain Behavior Is Encapsulated
  - template placeholder -> III. Tests Are Mandatory Quality Gates
  - template placeholder -> IV. Contracts, Migrations, and Generated Interfaces Stay Aligned
  - template placeholder -> V. Consistency and Performance Budgets Are Designed Up Front
- Added sections:
  - Engineering Standards
  - Delivery Workflow
- Removed sections:
  - None
- Templates requiring updates:
  - ✅ updated .specify/templates/plan-template.md
  - ✅ updated .specify/templates/spec-template.md
  - ✅ updated .specify/templates/tasks-template.md
  - ✅ no command templates directory present at .specify/templates/commands
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
consistently. Rationale: the current codebase uses contract checks, explicit
event publication, and rich domain models instead of anemic records.

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

**Version**: 1.0.0 | **Ratified**: 2026-03-15 | **Last Amended**: 2026-03-15
