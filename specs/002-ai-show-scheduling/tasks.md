# Tasks: AI Show Scheduling Bounded Context

**Input**: Design documents from `/specs/002-ai-show-scheduling/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are MANDATORY. Every story MUST include the automated tests needed
to prove the touched layers and contracts.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Parent build**: `pom.xml` at repository root
- **Scheduling module**: `scheduling/src/main/java`, `scheduling/src/main/resources`, `scheduling/src/test/java`
- **Seedwork module**: `seedwork/src/main/java`, `seedwork/src/main/resources`, `seedwork/src/test/java`
- **Migrations**: `scheduling/src/main/resources/db/migration`
- **OpenAPI contracts**: `scheduling/src/main/resources/static`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the new `scheduling` Maven module with build configuration, database schema, ORM mappings, and application configuration.

- [X] T001 Add `<module>scheduling</module>` to root `pom.xml` and create `scheduling/pom.xml` with dependencies on `seedwork`, Spring Boot starters (web, data-jpa, flyway, validation, actuator), H2, Anthropic Java SDK 2.18.0, springdoc/swagger, and test dependencies (spring-boot-starter-test, archunit-junit5)
- [X] T002 [P] Create Flyway migration `scheduling/src/main/resources/db/migration/V1_0__show.sql` with `show` table and `show_s` sequence per data-model.md schema
- [X] T003 [P] Copy OpenAPI spec from `specs/002-ai-show-scheduling/contracts/scheduling-openapi.yaml` to `scheduling/src/main/resources/static/scheduling-openapi.yaml` and configure `openapi-generator-maven-plugin` in `scheduling/pom.xml` to generate `ShowOperations` and `AgentOperations` interfaces with `apiPackage=com.github.scheduling.infrastructure.web` and `modelPackage=com.github.scheduling.infrastructure.web.representation`
- [X] T004 [P] Create domain ORM mapping `scheduling/src/main/resources/META-INF/domain/show.orm.xml` — map `Show` entity to `show` table with sequence generator `show_s`, embedded value objects (`ShowId`, `Movie` with `MovieId`, `Hall` with `HallId`), all identifier columns `updatable="false"`, and `<transient name="raisedEvents"/>` inherited from `AggregateRoot` mapped superclass
- [X] T005 [P] Create query projection ORM mapping `scheduling/src/main/resources/META-INF/query/show.orm.xml` — define named native queries `ShowDetailView.getShow` (by show_id), `ShowSummaryView.listShows` (with OFFSET/FETCH NEXT pagination), and corresponding `<sql-result-set-mapping>` constructor results
- [X] T006 [P] Create `scheduling/src/main/resources/application-default.yaml` with `spring.application.name: scheduling`, `spring.flyway.table: schema_history`, `spring.flyway.locations: [classpath:db/migration, classpath:seedwork/db/migration/outbox]`, `spring.jpa.hibernate.ddl-auto: none`, `spring.jpa.open-in-view: false`, `spring.jpa.mapping-resources` listing `META-INF/domain/show.orm.xml`, `META-INF/query/show.orm.xml`, and `seedwork/META-INF/domain/super-types.orm.xml`
- [X] T007 [P] Create `scheduling/src/main/resources/application-local.yaml` with H2 datasource (PostgreSQL mode), springdoc/swagger-ui config referencing `scheduling-openapi.yaml`, and `anthropic.api-key: ${ANTHROPIC_API_KEY}` property
- [X] T008 Create Spring Boot application class `scheduling/src/main/java/com/github/scheduling/SchedulingApplication.java` with `@SpringBootApplication`

**Checkpoint**: Module compiles, Flyway migration runs, Spring context boots with `mvn -B package -pl scheduling --file pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain model value objects, domain service interfaces, events, exceptions, and repository interface that ALL user stories depend on. Also test fixtures and base test classes.

**CRITICAL**: No user story work can begin until this phase is complete.

- [X] T009 [P] Create value object `scheduling/src/main/java/com/github/scheduling/domain/show/ShowId.java` — `record ShowId(String value) implements ValueObject` with `Contract.require(value != null)`
- [X] T010 [P] Create value object `scheduling/src/main/java/com/github/scheduling/domain/movie/MovieId.java` — `record MovieId(String value) implements ValueObject` with `Contract.require(value != null)`
- [X] T011 [P] Create value object `scheduling/src/main/java/com/github/scheduling/domain/movie/Movie.java` — `record Movie(MovieId movieId, String title, int runtimeMinutes) implements ValueObject` with `Contract.require` for non-null movieId, non-blank title, and runtimeMinutes > 0
- [X] T012 [P] Create value object `scheduling/src/main/java/com/github/scheduling/domain/hall/HallId.java` — `record HallId(String value) implements ValueObject` with `Contract.require(value != null)`
- [X] T013 [P] Create value object `scheduling/src/main/java/com/github/scheduling/domain/hall/Hall.java` — `record Hall(HallId hallId, String name, int seatCount) implements ValueObject` with `Contract.require` for non-null hallId, non-blank name, and seatCount > 0
- [X] T014 [P] Create domain service interface `scheduling/src/main/java/com/github/scheduling/domain/movie/MovieService.java` with `Movie movieFrom(MovieId movieId)` and `List<Movie> listMovies()`
- [X] T015 [P] Create domain service interface `scheduling/src/main/java/com/github/scheduling/domain/hall/HallService.java` with `Hall hallFrom(HallId hallId)` and `List<Hall> listHalls()`
- [X] T016 [P] Create abstract base event `scheduling/src/main/java/com/github/scheduling/domain/show/ShowEvent.java` — implements seedwork `Event`, final `ShowId showId` field, `Contract.require(showId != null)` in constructor
- [X] T017 [P] Create domain event `scheduling/src/main/java/com/github/scheduling/domain/show/ShowScheduled.java` — extends `ShowEvent`, final fields `MovieId movieId`, `HallId hallId`, `Instant scheduledAt`, all validated via `Contract.require`
- [X] T018 [P] Create exception `scheduling/src/main/java/com/github/scheduling/domain/show/ShowException.java` — extends `ProblemException`, private constructor, `NOT_FOUND_PROBLEM` (Problem.notFound), `OVERLAP_PROBLEM` (Problem.conflict), `PAST_SCHEDULE_PROBLEM` (Problem.conflict), factory methods `notFound()`, `overlap()`, `pastSchedule()`
- [X] T019 [P] Create domain service interface `scheduling/src/main/java/com/github/scheduling/domain/show/ShowSchedulingPolicy.java` — `@Service` annotated, method `void ensureNoOverlap(HallId hallId, Instant start, Instant end)` that throws `ShowException.overlap()` on conflict
- [X] T020 [P] Create repository interface `scheduling/src/main/java/com/github/scheduling/domain/show/ShowRepository.java` with `ShowId nextShowId()`, `Optional<Show> findByShowId(ShowId showId)`, and `void save(Show show)`
- [X] T021 Create Show aggregate root `scheduling/src/main/java/com/github/scheduling/domain/show/Show.java` — extends `AggregateRoot`, fields `ShowId showId`, `Instant scheduledAt`, `Movie movie`, `Hall hall`; constructor validates all fields via `Contract.require`, validates `scheduledAt` is in the future via `Contract.check(scheduledAt.isAfter(Instant.now()), ShowException::pastSchedule)`, raises `ShowScheduled` event; accessors `showId()`, `scheduledAt()`, `movie()`, `hall()`; `equals`/`hashCode` on `showId`; `private Long id` and `protected Show()` at bottom
- [X] T022 [P] Create mock service `scheduling/src/main/java/com/github/scheduling/infrastructure/service/movie/MockMovieService.java` — `@Service` implements `MovieService`, `movieFrom` returns deterministic `Movie` from movieId hash with runtimeMinutes (90-180 range), `listMovies` returns fixed list of 5 movies with generated IDs
- [X] T023 [P] Create mock service `scheduling/src/main/java/com/github/scheduling/infrastructure/service/hall/MockHallService.java` — `@Service` implements `HallService`, `hallFrom` returns deterministic `Hall` from hallId hash, `listHalls` returns fixed list of 5 halls with generated IDs
- [X] T024 [P] Create test fixture `scheduling/src/test/java/com/github/scheduling/domain/movie/MovieFixture.java` — `newMovie(String movieId)` returning `Movie` with "TestTitle" and 120 runtimeMinutes
- [X] T025 [P] Create test fixture `scheduling/src/test/java/com/github/scheduling/domain/hall/HallFixture.java` — `newHall(String hallId)` returning `Hall` with "TestName" and 150 seatCount
- [X] T026 [P] Create test fixture `scheduling/src/test/java/com/github/scheduling/domain/show/ShowFixture.java` — `newShow(String showId)` creating `Show` with future scheduledAt, MovieFixture and HallFixture defaults, then `releaseEvents(Consumers.empty())` to clear events
- [X] T027 [P] Create persistence test base `scheduling/src/test/java/com/github/scheduling/infrastructure/persistence/PersistenceTest.java` extending seedwork `PersistenceTest`
- [X] T028 [P] Create controller test base `scheduling/src/test/java/com/github/scheduling/infrastructure/web/ControllerTest.java` extending seedwork `ControllerTest`

**Checkpoint**: All domain types compile, fixtures create valid domain objects, `mvn -B compile -pl scheduling --file pom.xml` passes

---

## Phase 3: User Story 1 - Schedule a Show via AI Agent (Priority: P1) MVP

**Goal**: A cinema administrator can schedule a show through the AI agent. The Show aggregate is persisted, a ShowScheduled domain event is published, and the domain event is forwarded as a mocked integration event.

**Independent Test**: Invoke `scheduleShow` via command handler → Show persisted → ShowScheduled event captured. Send message to AI agent endpoint → agent uses `schedule_show` tool → returns confirmation.

### Tests for User Story 1

- [X] T029 [P] [US1] Create domain unit test `scheduling/src/test/java/com/github/scheduling/domain/show/ShowTest.java` — test `constructorShouldCreateShowAndRaiseShowScheduledEvent` (verify all fields, verify exactly one ShowScheduled event raised with correct data), `constructorWithPastScheduledAtShouldThrowShowException` (verify pastSchedule exception), `equalsShouldBeBasedOnShowId`, `hashCodeShouldBeBasedOnShowId`
- [X] T030 [P] [US1] Create application unit test `scheduling/src/test/java/com/github/scheduling/application/show/ShowCommandHandlerImplTest.java` — mock ShowRepository, MovieService, HallService, ShowSchedulingPolicy; test `scheduleShowShouldScheduleShow` (verify show captured via ArgumentCaptor has correct fields), `scheduleShowWithOverlapShouldThrowShowException` (verify policy called and exception propagated)
- [X] T031 [P] [US1] Create persistence integration test `scheduling/src/test/java/com/github/scheduling/infrastructure/persistence/show/JpaShowRepositoryTest.java` — extends scheduling PersistenceTest; test `saveShouldSaveShow` (write in one transaction, read in another, verify fields), `nextShowIdShouldReturnShowId` (verify format S0[0-9A-F]{16}), `findByShowIdShouldReturnShow`
- [X] T032 [P] [US1] Create architecture test `scheduling/src/test/java/com/github/scheduling/ArchitectureTest.java` — mirror booking ArchitectureTest rules for `com.github.scheduling` package: layered architecture enforcement, domain type constraints (Entity/Event/ValueObject/@Service/ProblemException/interface only), no JavaBean getters/setters, event/value object immutability

### Implementation for User Story 1

- [X] T03- [ ] T033 [P] [US1] Create command record `scheduling/src/main/java/com/github/scheduling/application/show/command/ScheduleShowCommand.java` — `record ScheduleShowCommand(Instant scheduledAt, String movieId, String hallId)`
- [X] T03- [ ] T034 [P] [US1] Create result record `scheduling/src/main/java/com/github/scheduling/application/show/command/ScheduleShowResult.java` — `record ScheduleShowResult(String showId)`
- [X] T03- [ ] T035 [US1] Create command handler interface `scheduling/src/main/java/com/github/scheduling/application/show/ShowCommandHandler.java` — `@Transactional` annotated interface with `ScheduleShowResult scheduleShow(ScheduleShowCommand command)`
- [X] T03- [ ] T036 [US1] Create command handler implementation `scheduling/src/main/java/com/github/scheduling/application/show/ShowCommandHandlerImpl.java` — `@Service`, constructor-injects `ShowRepository`, `MovieService`, `HallService`, `ShowSchedulingPolicy`; `scheduleShow` orchestrates: resolve movie via `movieService.movieFrom()`, resolve hall via `hallService.hallFrom()`, call `showSchedulingPolicy.ensureNoOverlap(hallId, scheduledAt, scheduledAt + movie.runtimeMinutes)`, create `Show(showRepository.nextShowId(), scheduledAt, movie, hall)`, `showRepository.save(show)`, return `ScheduleShowResult(show.showId().value())`
- [X] T03- [ ] T037 [US1] Create JPA repository `scheduling/src/main/java/com/github/scheduling/infrastructure/persistence/show/JpaShowRepository.java` — extends `JpaAggregateRootSupport`, `Repository<Show, Long>`, `ShowRepository`; default `nextShowId()` returns `new ShowId("S0%016X".formatted(RandomSupport.nextLong()))`; default `save(Show)` delegates to `saveAndPublishEvents(show.showId().value(), show)`
- [X] T03- [ ] T038 [US1] Create JPA scheduling policy `scheduling/src/main/java/com/github/scheduling/infrastructure/persistence/show/JpaShowSchedulingPolicy.java` — `@Service` implements `ShowSchedulingPolicy`, injects `EntityManager`, `ensureNoOverlap` executes native SQL query to check for overlapping shows in the same hall (WHERE hall_id = :hallId AND scheduled_at < :end AND scheduled_at + movie_runtime_minutes * INTERVAL '1 minute' > :start), throws `ShowException.overlap()` if count > 0
- [X] T03- [ ] T039 [US1] Create mock integration event publisher `scheduling/src/main/java/com/github/scheduling/infrastructure/event/ShowScheduledIntegrationEventPublisher.java` — `@Component` with `@EventListener` for `ShowScheduled` domain event, logs integration event at INFO level with show/movie/hall/time details
- [X] T040 [US1] Create AI scheduling agent `scheduling/src/main/java/com/github/scheduling/infrastructure/agent/SchedulingAgent.java` — `@Service`, injects `AnthropicClient` (configured via `@Value("${anthropic.api-key}")`) and `ShowCommandHandler`; defines `schedule_show` tool with input schema matching contracts/agent-tools.md; `processMessage(String message)` method sends user message to Claude API with tools, loops on tool_use responses (invokes command handler, sends tool result back), returns final text response; system prompt describes the agent as a cinema scheduling assistant
- [X] T041 [US1] Create agent controller `scheduling/src/main/java/com/github/scheduling/infrastructure/web/agent/AgentController.java` — `@RestController` implementing generated `AgentOperations` interface, injects `SchedulingAgent`, delegates `sendAgentMessage` to `schedulingAgent.processMessage(request.getMessage())`, returns `AgentMessageResponse` with agent's text reply
- [X] T042 [US1] Verify `mvn -B package -pl scheduling --file pom.xml` passes with all US1 tests green

**Checkpoint**: Show scheduling works end-to-end via command handler and AI agent. ShowScheduled domain event published and forwarded as mock integration event.

---

## Phase 4: User Story 2 - Query Available Halls and Movies (Priority: P2)

**Goal**: The AI agent can list available halls and movies via query handler tools, enabling informed scheduling decisions.

**Independent Test**: Call `listHalls` / `listMovies` query handlers and verify mock data returned. Send "list halls" message to agent and verify tool invocation.

### Tests for User Story 2

- [X] T043 [P] [US2] Create application unit test `scheduling/src/test/java/com/github/scheduling/application/show/HallQueryHandlerImplTest.java` — test `listHallsShouldReturnAllHalls` verifying mock service data is returned as `HallView` list
- [X] T044 [P] [US2] Create application unit test `scheduling/src/test/java/com/github/scheduling/application/show/MovieQueryHandlerImplTest.java` — test `listMoviesShouldReturnAllMovies` verifying mock service data is returned as `MovieView` list

### Implementation for User Story 2

- [X] T045 [P] [US2] Create query records `scheduling/src/main/java/com/github/scheduling/application/show/query/ListHallsQuery.java` and `scheduling/src/main/java/com/github/scheduling/application/show/query/HallView.java` — `record ListHallsQuery()`, `record HallView(String hallId, String name, int seatCount)`
- [X] T046 [P] [US2] Create query records `scheduling/src/main/java/com/github/scheduling/application/show/query/ListMoviesQuery.java` and `scheduling/src/main/java/com/github/scheduling/application/show/query/MovieView.java` — `record ListMoviesQuery()`, `record MovieView(String movieId, String title, int runtimeMinutes)`
- [X] T047 [P] [US2] Create query handler interface and implementation `scheduling/src/main/java/com/github/scheduling/application/show/HallQueryHandler.java` and `scheduling/src/main/java/com/github/scheduling/application/show/HallQueryHandlerImpl.java` — `@Transactional(readOnly = true)` interface with `List<HallView> listHalls(ListHallsQuery query)`; implementation `@Service` injects `HallService`, maps `Hall` to `HallView`
- [X] T048 [P] [US2] Create query handler interface and implementation `scheduling/src/main/java/com/github/scheduling/application/show/MovieQueryHandler.java` and `scheduling/src/main/java/com/github/scheduling/application/show/MovieQueryHandlerImpl.java` — `@Transactional(readOnly = true)` interface with `List<MovieView> listMovies(ListMoviesQuery query)`; implementation `@Service` injects `MovieService`, maps `Movie` to `MovieView`
- [X] T049 [US2] Add `list_halls` and `list_movies` tools to `scheduling/src/main/java/com/github/scheduling/infrastructure/agent/SchedulingAgent.java` — define tool schemas per contracts/agent-tools.md, handle tool_use by invoking `HallQueryHandler.listHalls()` and `MovieQueryHandler.listMovies()`, return JSON tool results
- [X] T050 [US2] Verify `mvn -B package -pl scheduling --file pom.xml` passes with all US1 and US2 tests green

**Checkpoint**: Agent can list halls and movies. Halls and movies mock data flows through query handlers to agent tool responses.

---

## Phase 5: User Story 3 - Query Scheduled Shows (Priority: P3)

**Goal**: The AI agent can list previously scheduled shows. REST endpoints also expose show data for direct HTTP access.

**Independent Test**: Schedule shows via command handler, then query via `listShows`/`getShow` query handlers and verify results. Hit REST endpoints `GET /shows` and `GET /shows/{show_id}`.

### Tests for User Story 3

- [X] T051 [P] [US3] Create persistence integration test `scheduling/src/test/java/com/github/scheduling/infrastructure/persistence/show/JpaShowQueryHandlerTest.java` — extends scheduling PersistenceTest; test `getShowShouldReturnShowDetailView` (save a Show, query by showId, verify all fields), `listShowsShouldReturnShowSummaryViews` (save multiple Shows, verify pagination)
- [X] T052 [P] [US3] Create controller integration test `scheduling/src/test/java/com/github/scheduling/infrastructure/web/show/ShowControllerTest.java` — extends scheduling ControllerTest; `@MockitoBean` for ShowQueryHandler; test `getShowShouldReturnShowDetailResponse` (mock query handler, GET /shows/{show_id}, verify 200 + JSON body), `getShowWithUnknownIdShouldReturn404`, `listShowsShouldReturnListShowsResponse` (verify offset/limit pagination)

### Implementation for User Story 3

- [X] T053 [P] [US3] Create query records `scheduling/src/main/java/com/github/scheduling/application/show/query/GetShowQuery.java`, `scheduling/src/main/java/com/github/scheduling/application/show/query/ListShowsQuery.java`, `scheduling/src/main/java/com/github/scheduling/application/show/query/ShowDetailView.java`, `scheduling/src/main/java/com/github/scheduling/application/show/query/ShowSummaryView.java` — GetShowQuery(String showId), ListShowsQuery(long offset, int limit), ShowDetailView(all show fields), ShowSummaryView(showId, scheduledAt, movieTitle, hallName)
- [X] T054 [US3] Create query handler interface `scheduling/src/main/java/com/github/scheduling/application/show/ShowQueryHandler.java` — `@Transactional(readOnly = true)` with `ShowDetailView getShow(GetShowQuery query)` and `List<ShowSummaryView> listShows(ListShowsQuery query)`
- [X] T055 [US3] Create query handler implementation `scheduling/src/main/java/com/github/scheduling/infrastructure/persistence/show/JpaShowQueryHandler.java` — `@Service` injects `JpaShowRepository`, `getShow` delegates to `repository.getShow(query.showId()).orElseThrow(ShowException::notFound)`, `listShows` delegates to `repository.listShows(query.offset(), query.limit())`
- [X] T056 [US3] Add `@NativeQuery` methods to `scheduling/src/main/java/com/github/scheduling/infrastructure/persistence/show/JpaShowRepository.java` — `Optional<ShowDetailView> getShow(@Param("show_id") String showId)` and `List<ShowSummaryView> listShows(@Param("offset") long offset, @Param("limit") int limit)` referencing the named native queries from show query ORM XML
- [X] T057 [US3] Create show mapper `scheduling/src/main/java/com/github/scheduling/infrastructure/web/show/ShowMapper.java` — `@Component` with `toShowDetailResponse(ShowDetailView)` and `toListShowsResponse(List<ShowSummaryView>)` mapping view projections to generated OpenAPI response types
- [X] T058 [US3] Create show controller `scheduling/src/main/java/com/github/scheduling/infrastructure/web/show/ShowController.java` — `@RestController` implementing generated `ShowOperations` interface, injects `ShowQueryHandler` and `ShowMapper`, delegates `getShow` and `listShows` to query handler + mapper
- [X] T059 [US3] Add `list_shows` tool to `scheduling/src/main/java/com/github/scheduling/infrastructure/agent/SchedulingAgent.java` — define tool schema per contracts/agent-tools.md with optional offset/limit parameters, handle tool_use by invoking `ShowQueryHandler.listShows()`, return JSON tool result
- [X] T060 [US3] Verify `mvn -B package -pl scheduling --file pom.xml` passes with all US1, US2, and US3 tests green

**Checkpoint**: All three user stories functional. Shows can be scheduled via agent, halls/movies queried, and scheduled shows listed via agent and REST endpoints.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Full build verification, cross-module integration, and final quality checks.

- [X] T061 Verify full build `mvn -B package --file pom.xml` passes (all modules: seedwork, booking, scheduling)
- [X] T062 [P] Review ArchitectureTest coverage — ensure all domain types in `com.github.scheduling.domain` are covered by entity/event/value-object rules, and infrastructure-to-domain bypass is blocked
- [X] T063 [P] Verify OpenAPI spec validation — ensure `scheduling/src/main/resources/static/scheduling-openapi.yaml` generates valid interfaces and that controllers implement them without compile errors
- [X] T064 Run quickstart.md smoke test — boot application with local profile, hit `POST /scheduling/agent`, `GET /shows`, verify responses

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) completion — BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational (Phase 2) — this is the MVP
- **User Story 2 (Phase 4)**: Depends on Foundational (Phase 2) — can run in parallel with US1 for query handlers, but agent tool integration depends on SchedulingAgent from US1
- **User Story 3 (Phase 5)**: Depends on Foundational (Phase 2) — can run in parallel with US1/US2 for query handler + REST, but agent tool integration depends on SchedulingAgent from US1
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2. Creates the SchedulingAgent and core scheduling flow. **Other stories depend on the SchedulingAgent created here** for adding their tools.
- **User Story 2 (P2)**: Query handlers (T045-T048) can be built in parallel with US1. Agent tool registration (T049) depends on SchedulingAgent from US1 (T040).
- **User Story 3 (P3)**: Query handlers + REST (T053-T058) can be built in parallel with US1/US2. Agent tool registration (T059) and query ORM depend on JpaShowRepository from US1 (T037).

### Within Each User Story

- Tests MUST be written and fail before implementation
- Domain and application types before infrastructure adapters
- Contract and migration assets before dependent integration wiring
- Core implementation before cross-layer integration
- Story complete before moving to next priority

### Parallel Opportunities

- **Phase 1**: T002-T007 all touch different files — full parallelism
- **Phase 2**: T009-T020 are all independent value objects/interfaces — full parallelism; T021 (Show aggregate) depends on T009-T020; T022-T028 can parallel with T021
- **US1**: T029-T032 (tests) in parallel; T033-T034 in parallel; T037-T039 in parallel after T035-T036
- **US2**: T043-T044 (tests) in parallel; T045-T048 (implementation) in parallel
- **US3**: T051-T052 (tests) in parallel; T053 in parallel with T054; T057-T058 after T054-T056

---

## Parallel Example: User Story 1

```bash
# Launch all tests in parallel (they target different files):
T029: ShowTest.java (domain unit test)
T030: ShowCommandHandlerImplTest.java (application unit test)
T031: JpaShowRepositoryTest.java (persistence integration test)
T032: ArchitectureTest.java (architecture test)

# Launch parallel implementation tasks:
T033 + T034: ScheduleShowCommand.java + ScheduleShowResult.java (different files)
T037 + T038 + T039: JpaShowRepository + JpaShowSchedulingPolicy + IntegrationEventPublisher (different files)
```

## Parallel Example: User Story 2

```bash
# Tests in parallel:
T043: HallQueryHandlerImplTest.java
T044: MovieQueryHandlerImplTest.java

# Implementation in parallel:
T045 + T046: ListHallsQuery/HallView + ListMoviesQuery/MovieView (different files)
T047 + T048: HallQueryHandler + MovieQueryHandler (different files)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test scheduling via command handler and AI agent
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Module boots, domain model ready
2. Add User Story 1 → AI agent can schedule shows → Deploy/Demo (MVP!)
3. Add User Story 2 → Agent can query halls/movies → Deploy/Demo
4. Add User Story 3 → Agent can list shows + REST API available → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (core scheduling + agent)
   - Developer B: User Story 2 query handlers (T045-T048) in parallel
   - Developer C: User Story 3 query handlers + REST (T053-T058) in parallel
3. After US1 agent is ready: B adds tools (T049), C adds tools (T059)
4. Polish phase together

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- The `ShowFixture.newShow()` must use a future `scheduledAt` (e.g., `Instant.now().plus(7, ChronoUnit.DAYS)`) since the Show constructor validates future dates
- The SchedulingAgent tool-use loop must handle multiple rounds of tool calls before Claude returns a final text response
- Anthropic API key must be set as `ANTHROPIC_API_KEY` env var for AI agent tests/demo
