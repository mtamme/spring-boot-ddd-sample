# Research: AI Show Scheduling Bounded Context

**Branch**: `002-ai-show-scheduling` | **Date**: 2026-03-26

## R1: Anthropic Java SDK for Claude API Tool Use

**Decision**: Use the official `com.anthropic:anthropic-java` SDK (version 2.18.0) for Claude LLM integration.

**Rationale**: This is the official Anthropic Java SDK with first-class support for tool use (function calling),
builder-pattern API construction, and type-safe responses. It handles the full tool-use loop: sending tool definitions,
receiving tool_use blocks, and sending tool results back.

**Key API patterns**:

- Client: `AnthropicOkHttpClient.builder().apiKey(key).build()` (or `.fromEnv()` for `ANTHROPIC_API_KEY`)
- Tool definition: `Tool.builder().name("...").description("...").inputSchema(schema).build()` with
  `JsonValue.from(Map)` for properties
- Tool use detection: Check `message.stopReason() == StopReason.TOOL_USE`, iterate `ContentBlock` for `ToolUseBlock`
- Tool result: Send back via `ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()...)`
- Conversation loop: Accumulate messages (user → assistant with tool_use → user with tool_result → assistant with final
  text)

**Alternatives considered**:

- Raw HTTP client: Rejected — SDK provides type safety, handles serialization, and simplifies the tool-use loop
- LangChain4j / Spring AI: Rejected — adds unnecessary abstraction layer; the direct SDK gives full control and aligns
  with the infrastructure-layer placement

**Maven dependency**:

```xml
<dependency>
    <groupId>com.anthropic</groupId>
    <artifactId>anthropic-java</artifactId>
    <version>2.18.0</version>
</dependency>
```

## R2: Integration Event Publication Pattern

**Decision**: Use a `MockIntegrationEventPublisher` in the infrastructure layer that listens for the `ShowScheduled`
domain event and logs/records the integration event. No actual message broker.

**Rationale**: The user explicitly requested that integration event publication be mocked. The existing outbox pattern
in seedwork provides a full transactional outbox with message persistence, but that's overkill for the mock. A simple
Spring `@EventListener` in infrastructure that converts domain events to integration events and logs them is sufficient.
When a real broker is needed later, the mock can be replaced with a Kafka/RabbitMQ publisher.

**Design**:

- Infrastructure layer has a `ShowScheduledIntegrationEventPublisher` that listens for `ShowScheduled` domain events via
  `@EventListener`
- Converts domain event to an integration event representation (could be a simple record or JSON)
- Logs the integration event at INFO level (for observability)
- No outbox, no broker — just logging

**Alternatives considered**:

- Reusing seedwork's outbox pattern: Rejected — outbox is designed for reliable delivery with retry; the mock only needs
  logging
- Application-layer event handler: Rejected — integration event publication is an infrastructure concern, not
  cross-aggregate orchestration

## R3: Hall Overlap / Show Scheduling Constraint

**Decision**: Implement overlap checking via a domain service interface `ShowSchedulingPolicy` in the domain layer, with
a JPA-backed implementation in infrastructure.

**Rationale**: The overlap check ("no two shows in the same hall at overlapping times") is a business rule that must
live in the domain. However, it requires querying existing shows from the database. A domain service interface bridges
this: the domain defines the contract, infrastructure provides the query.

**Design**:

- Domain service: `ShowSchedulingPolicy` with `ensureNoOverlap(HallId hallId, Instant start, Instant end)`
- Infrastructure: `JpaShowSchedulingPolicy` queries the show table for overlapping time ranges
- Command handler orchestrates: resolve movie/hall → call `ensureNoOverlap` → create Show → save
- Overlap is defined as: existing show's `[scheduledAt, scheduledAt + movie.runtime)` intersects with new show's
  `[scheduledAt, scheduledAt + movie.runtime)`

**Alternatives considered**:

- Putting overlap logic in the command handler: Rejected — business logic in handlers violates constitution
- Passing existing shows to the aggregate constructor: Rejected — couples aggregate construction to persistence queries;
  awkward API

## R4: AI Agent Entry Point

**Decision**: Expose the AI scheduling agent via a REST endpoint `POST /scheduling/agent` that accepts a
natural-language message and returns the agent's response.

**Rationale**: A REST endpoint is the simplest entry point that integrates naturally with Spring Boot, supports testing
via MockMvc, and follows the existing controller pattern. The endpoint wraps a single turn of conversation with the AI
agent (send message → agent processes with tools → return response).

**Design**:

- OpenAPI spec defines the endpoint: `POST /scheduling/agent` with `{ "message": "..." }` request body
- Returns `{ "response": "..." }` with the agent's natural language reply
- Controller delegates to the AI agent in infrastructure
- The agent handles the full tool-use loop internally (may make multiple Claude API calls)

**Alternatives considered**:

- CLI entry point: Rejected — less testable, less standard for Spring Boot
- No REST endpoint (programmatic only): Rejected — needs an entry point for testing and demonstration

## R5: Module Structure for New Bounded Context

**Decision**: Create a new `scheduling` Maven module alongside `booking` and `seedwork`, following identical
conventions.

**Rationale**: The constitution explicitly says "New bounded-context code MUST be added to an existing module or a
deliberately planned new module." A separate module ensures bounded context independence (no coupling to `booking`) and
allows ArchUnit to enforce layering rules independently.

**Key structure decisions**:

- Root package: `com.github.scheduling`
- Module depends on: `seedwork` (for AggregateRoot, Event, etc.), `anthropic-java` SDK
- Module does NOT depend on: `booking`
- H2 database for local development (same as booking)
- Own Flyway migrations under `scheduling/src/main/resources/db/migration/`
- Own ORM mappings under `scheduling/src/main/resources/META-INF/domain/` and `META-INF/query/`
- Own ArchUnit test mirroring booking's `ArchitectureTest.java`

**Alternatives considered**:

- Adding to booking module: Rejected — violates bounded context independence; booking and scheduling are separate
  concerns

## R6: Clock Injection for Time-Dependent Domain Validation (Constitution v1.6.0)

**Decision**: Pass `Instant now` as a parameter to the `Show` aggregate constructor. The application-layer
`ShowCommandHandlerImpl` injects the seedwork `Clock` bean and passes `clock.instant()` when constructing the Show.

**Rationale**: Constitution v1.6.0 forbids direct `Instant.now()` calls in application and infrastructure code. The
domain layer must remain technology-agnostic (no `Clock` dependency), but the `Show` constructor currently calls
`Instant.now()` to validate that `scheduledAt` is in the future. Passing the current time as a constructor parameter
keeps the domain pure while satisfying the Clock injection rule. The command handler — which lives in the application
layer — is the natural injection point for the `Clock` bean.

**Impact on existing code**:

- `Show.java`: Constructor signature changes from `Show(ShowId, Instant scheduledAt, Movie, Hall)` to
  `Show(ShowId, Instant scheduledAt, Movie, Hall, Instant now)`. Validation becomes
  `Contract.check(scheduledAt.isAfter(now), ShowException::pastSchedule)`.
- `ShowCommandHandlerImpl.java`: Inject `Clock` via constructor. Call `clock.instant()` and pass to `new Show(...)`.
- `ShowFixture.java`: Use a fixed `Instant` (e.g., `Instant.parse("2026-01-01T00:00:00Z")`) as the `now` parameter, with
  `scheduledAt` set 7 days after that fixed instant.
- `ShowTest.java`: Use fixed instants for both `now` and `scheduledAt`, making tests deterministic.
- `ShowCommandHandlerImplTest.java`: Mock `Clock` or pass `Clock.fixed(...)` to the handler.
- `JpaShowRepositoryTest.java` and other integration tests: Override `Clock` bean with `Clock.fixed(...)` via a test
  configuration class, following the constitution mandate that "integration tests that boot the Spring context MUST
  override the auto-configured Clock bean with a fixed instance via a test configuration class."

**Alternatives considered**:

- Injecting `Clock` directly into the `Show` aggregate: Rejected — domain classes must remain technology-agnostic;
  `Clock` is a `java.time` class but its injection implies framework wiring in the domain.
- Using a static `TimeProvider` utility in seedwork domain: Rejected — hidden static dependency; harder to test; less
  explicit than parameter passing.
- Keeping `Instant.now()` in the domain and documenting as exception: Rejected — constitution is explicit about
  forbidding `Instant.now()` and the spirit of the rule (testability, determinism) clearly applies to domain code even
  though the literal text says "application and infrastructure code."

## R7: POST Collection Resource URI for Agent Endpoint (Constitution v1.6.0)

**Decision**: Rename `POST /scheduling/agent` to `POST /scheduling/agent/messages` and return `201 Created` instead of
`200 OK`.

**Rationale**: Constitution v1.6.0 added an explicit rule that POST requests MUST target a collection resource URI. The
current `POST /scheduling/agent` endpoint is not modeled as a collection resource. By renaming to
`/scheduling/agent/messages`, each interaction is modeled as creating a new "agent message" resource in the messages
collection. The response body remains `{ "response": "..." }` but the status code changes to `201 Created` to align with
POST semantics.

**Impact on existing code**:

- `scheduling-openapi.yaml` (both in `src/main/resources/static/` and `specs/contracts/`): Path changes from
  `/scheduling/agent` to `/scheduling/agent/messages`. Response status changes from `200` to `201 Created`.
- `AgentController.java`: Return `ResponseEntity.created(...)` or `ResponseEntity.status(HttpStatus.CREATED)` instead of
  `ResponseEntity.ok(...)`.
- Generated `AgentOperations` interface: Regenerated from updated OpenAPI spec.
- `quickstart.md`: Update curl example URL.

**Alternatives considered**:

- Keeping `POST /scheduling/agent` as-is and documenting as justified exception: Considered — the endpoint is not a
  resource creation endpoint in the traditional sense. However, the constitution rule is unambiguous ("never an
  individual resource or action endpoint"), and modeling as a message collection is a clean REST-compatible design.
- Using `PUT` for idempotency: Rejected — agent messages are not idempotent; each message creates a new interaction with
  potentially different tool invocations and results.
- Using `GET` with request body: Rejected — while semantically closer (query/response), GET with a body is non-standard
  and poorly supported by clients.
