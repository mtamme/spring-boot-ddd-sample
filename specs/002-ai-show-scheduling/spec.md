# Feature Specification: AI Show Scheduling Bounded Context

**Feature Branch**: `002-ai-show-scheduling`
**Created**: 2026-03-26
**Status**: Draft
**Input**: User description: "Implement a new bounded context responsible for scheduling shows with the help of an AI agent using the Claude LLM API. The agent lives in the infrastructure layer and provides command handler and query handler based tools to the LLM. The domain model implements business logic. The application layer implements command and query handlers. The main aggregate is the Show. When a show is scheduled, a ShowScheduled event is published. Halls and movies access should be mocked similar to existing booking context mocks."

## Clarifications

### Session 2026-03-26

- Q: How should the ShowScheduled domain event reach other bounded contexts (e.g., booking)? → A: The ShowScheduled domain event is converted to an integration event and published via the infrastructure layer. The publication mechanism is mocked for now (no Kafka or similar broker); the actual transport will be implemented in the future.
- Q: Can the same hall have overlapping shows at the same time? → A: No. The domain enforces that a hall cannot have two shows with overlapping times.
- Q: How are show overlaps determined (duration)? → A: Duration is derived from the movie's runtime. The Movie model must expose a runtime attribute, and the mock movie service must provide runtime data.
- Q: Can a scheduled show be cancelled or rescheduled? → A: No. Shows are immutable once scheduled. Cancel and reschedule capabilities are out of scope and can be added as a future feature.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Schedule a Show via AI Agent (Priority: P1)

A cinema administrator requests the AI scheduling agent to schedule a new show. The administrator describes what they want in natural language (e.g., "Schedule Movie X in Hall Y for tomorrow at 7 PM"). The AI agent uses its available tools -- which map to the scheduling context's command and query handlers -- to look up available halls and movies, then schedules the show. When the show is successfully created, a ShowScheduled event is published.

**Why this priority**: This is the core capability of the entire bounded context. Without AI-driven show scheduling, the context has no reason to exist.

**Independent Test**: Can be fully tested by sending a scheduling request to the AI agent and verifying that a show is persisted with the correct hall, movie, and time, and that a ShowScheduled event is published.

**Acceptance Scenarios**:

1. **Given** the system has halls and movies available (via mocks), **When** the AI agent receives a request to schedule a show with a specific movie, hall, and date/time, **Then** a new Show aggregate is created and persisted, and a ShowScheduled event is published containing the show identifier, movie, hall, and scheduled time.
2. **Given** a scheduling request references a non-existent movie or hall, **When** the AI agent attempts to schedule the show, **Then** the system rejects the request with a meaningful error indicating the resource was not found.
3. **Given** the AI agent has tools for querying halls and movies, **When** the agent processes a natural-language scheduling request, **Then** the agent uses the query tools to look up available halls and movies before invoking the schedule command.

---

### User Story 2 - Query Available Halls and Movies (Priority: P2)

A cinema administrator asks the AI agent what halls and movies are available for scheduling. The agent uses its query handler tools to retrieve the list of halls and movies and presents the results.

**Why this priority**: Querying available resources is essential for informed scheduling decisions but is a supporting capability to the primary scheduling flow.

**Independent Test**: Can be tested by asking the agent to list halls or movies and verifying the correct mock data is returned.

**Acceptance Scenarios**:

1. **Given** mock hall data is available, **When** the AI agent is asked to list available halls, **Then** the agent returns a list of halls with their names and seating capacity.
2. **Given** mock movie data is available, **When** the AI agent is asked to list available movies, **Then** the agent returns a list of movies with their titles.

---

### User Story 3 - Query Scheduled Shows (Priority: P3)

A cinema administrator asks the AI agent to list shows that have already been scheduled. The agent queries the scheduling context and returns a summary of existing shows.

**Why this priority**: Reviewing existing schedules is useful for avoiding conflicts and provides visibility, but is secondary to the ability to create new schedules.

**Independent Test**: Can be tested by scheduling one or more shows, then querying the list and verifying all scheduled shows appear with correct details.

**Acceptance Scenarios**:

1. **Given** one or more shows have been scheduled, **When** the AI agent is asked to list scheduled shows, **Then** the agent returns a list of shows including the movie title, hall name, and scheduled date/time.
2. **Given** no shows have been scheduled yet, **When** the AI agent is asked to list scheduled shows, **Then** the agent returns an empty result indicating no shows are scheduled.

---

### Edge Cases

- What happens when the AI agent's LLM call fails or times out? The system should surface an error to the caller without creating a partial show.
- What happens when the scheduling request is ambiguous (e.g., no hall specified)? The AI agent should use its tools to present options or ask for clarification via its conversational flow.
- What happens when a show is scheduled for a date/time in the past? The domain should reject the scheduling with a validation error.
- What happens when a show is scheduled in a hall that already has a show at an overlapping time? The domain rejects the scheduling with a conflict error.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a new `scheduling` bounded context as a separate module, following the same layered architecture as the existing `booking` context (domain, application, infrastructure layers).
- **FR-002**: System MUST implement a Show aggregate root in the scheduling domain layer that represents a scheduled show, containing at minimum a show identifier, the scheduled date/time, associated movie, and associated hall.
- **FR-003**: System MUST publish a ShowScheduled domain event when a new show is successfully created and persisted.
- **FR-010**: System MUST convert the ShowScheduled domain event into an integration event and publish it via the infrastructure layer so that other bounded contexts (e.g., booking) can consume it in the future. The integration event publication mechanism MUST be mocked for now without implementing an actual message broker.
- **FR-004**: System MUST implement command handlers in the application layer for scheduling a show (at minimum a ScheduleShow command).
- **FR-005**: System MUST implement query handlers in the application layer for listing halls, listing movies, and listing scheduled shows.
- **FR-006**: System MUST implement an AI scheduling agent in the infrastructure layer that integrates with the Claude LLM API and exposes command and query handlers as tools the LLM can invoke.
- **FR-007**: System MUST provide mock implementations for hall and movie data access (following the same mock service pattern used in the booking context), simulating a future halls/movies bounded context.
- **FR-008**: System MUST validate that a show cannot be scheduled in the past; the domain layer enforces this invariant.
- **FR-011**: System MUST enforce that a hall cannot have two shows with overlapping times. A show's time span is determined by its scheduled start time plus the movie's runtime. The domain layer validates this constraint during scheduling, requiring a check against existing shows for the same hall.
- **FR-009**: The AI agent tools MUST map directly to the application layer's command and query handlers, ensuring the agent cannot bypass the application layer to access the domain directly.

### Architecture & Consistency Constraints *(mandatory)*

- **AC-001**: This feature introduces a new `scheduling` module alongside `booking` and `seedwork`. The `scheduling` module depends on `seedwork` but NOT on `booking`.
- **AC-002**: Dependency direction follows the same rules enforced by ArchUnit in `booking`: domain depends on nothing outside `seedwork.domain` and `seedwork.core`; application depends only on domain; infrastructure depends on domain and application. The AI agent (infrastructure) accesses domain logic only through application-layer command/query handlers.
- **AC-003**: The feature requires new ORM mappings for the Show aggregate in the scheduling context, a Flyway migration for the scheduling schema, and infrastructure configuration for the Claude LLM API client.
- **AC-004**: The Show aggregate enforces scheduling invariants (e.g., future date requirement) at construction time. Query handlers provide explicit, bounded result sets for halls, movies, and shows.

### Testing Expectations *(mandatory)*

- **TE-001**: Unit tests MUST cover Show aggregate creation (including invariant enforcement and ShowScheduled event publication). Integration tests MUST cover the command handler scheduling flow end-to-end. Architecture tests (ArchUnit) MUST be added for the scheduling module enforcing the same layered dependency rules as booking.
- **TE-002**: User Story 1 is proven complete when an integration test invokes ScheduleShow via the command handler, persists the Show, and a ShowScheduled event is captured. User Story 2 is proven when query handler tests return mock hall/movie data. User Story 3 is proven when a list-shows query returns previously scheduled shows.
- **TE-003**: The AI agent infrastructure integration (LLM API calls) may be tested with a stubbed LLM client to avoid external API dependencies in CI. The mock hall/movie services do not require separate integration tests since they follow the established booking context pattern.

### Key Entities

- **Show**: The main aggregate root of the scheduling context. Represents a scheduled cinema show. Key attributes: unique show identifier, scheduled date/time, associated movie (including runtime for overlap calculation), associated hall.
- **ShowScheduled (Domain Event)**: Domain event published when a Show is successfully created. Contains the show identifier, movie, hall, and scheduled date/time. Converted to an integration event in the infrastructure layer for cross-bounded-context communication.
- **ShowScheduled (Integration Event)**: Infrastructure-layer representation of the ShowScheduled domain event, intended for publication to external bounded contexts. Publication is mocked for now (no broker); the mock logs or records the event for observability.
- **Hall**: Value object (or reference) representing a cinema hall with a name and seating capacity. Sourced from a mock service simulating the future halls bounded context.
- **Movie**: Value object (or reference) representing a movie with a title and runtime (duration). Sourced from a mock service simulating the future movies bounded context. The runtime is used to calculate show end times for overlap detection.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An administrator can schedule a show through the AI agent in a single conversational interaction, from request to confirmed schedule.
- **SC-002**: Every successfully scheduled show results in exactly one ShowScheduled event being published, verifiable through event listeners or logs.
- **SC-003**: The AI agent correctly invokes the appropriate command/query handler tools for at least 90% of well-formed natural-language scheduling requests.
- **SC-004**: All scheduling requests (command handler invocations) complete within 5 seconds, excluding LLM response time.
- **SC-005**: The scheduling context passes all ArchUnit layering rules, confirming no architectural violations between domain, application, and infrastructure layers.

## Assumptions

- The scheduling context's Show aggregate is distinct from the booking context's Show aggregate. They are separate bounded contexts with separate domain models, even though both involve the concept of a "show."
- The Claude LLM API credentials will be configured via application properties or environment variables. The spec does not prescribe a specific authentication mechanism.
- The mock hall service will return a fixed set of halls (similar to booking's mock that generates deterministic hall data from an ID). The mock movie service will follow the same pattern.
- The AI agent's conversational interface (how it receives user requests) will be determined during planning. It could be exposed via a REST endpoint, a CLI, or another entry point.
- The halls and movies mock services in the scheduling context are independent of (not shared with) the booking context's mocks, maintaining bounded context independence.
- Shows are immutable once scheduled. There are no cancel or reschedule operations in this iteration. The Show aggregate has no state transitions beyond creation.
