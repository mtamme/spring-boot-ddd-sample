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

- [ ] Module and layer impact is explicit (`booking`: `infrastructure -> application -> domain`; `seedwork`: `infrastructure -> domain -> core`).
- [ ] Domain changes keep business rules in domain types and preserve entity/event/value-object modeling rules.
- [ ] Required automated tests are identified for every touched layer, including ArchUnit when structure changes.
- [ ] OpenAPI specs, Flyway migrations, ORM/query mappings, and generated interfaces are accounted for when contracts or persistence change.
- [ ] Performance budget is documented, including endpoint/query latency target, bounded result expectations, and any N+1 or chatty-call risks.
- [ ] All precondition checks use `Contract.require()` and all invariant checks use `Contract.check()`; domain exceptions extend `ProblemException` with static factory methods; idempotent state transitions use an early-return guard after the `Contract.check()`.
- [ ] Naming follows layer conventions: noun accessors and `is<State>()` predicates in domain; `<verb><Noun>(<Command>)` in command handlers; `get<Entity>` / `list<Entities>` / `search<Entities>` in query handlers; `on<EventType>` in event handlers; `<Noun>DetailView` / `<Noun>SummaryView` for view projections; `to<TargetType>()` in mappers; `<method>With<State>Should<Behavior>()` in tests; `<Aggregate>Fixture` factory methods in test fixtures.

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
