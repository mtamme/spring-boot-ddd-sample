# Data Model: AI Show Scheduling Bounded Context

**Branch**: `002-ai-show-scheduling` | **Date**: 2026-03-26

## Entities

### Show (Aggregate Root)

The primary aggregate of the scheduling bounded context. Represents a cinema show scheduled at a specific time in a
specific hall for a specific movie.

| Field       | Type    | Description                                       | Constraints                                  |
|-------------|---------|---------------------------------------------------|----------------------------------------------|
| showId      | ShowId  | Unique domain identifier                          | Required, immutable, format `S0[0-9A-F]{16}` |
| scheduledAt | Instant | Date/time when the show starts                    | Required, must be in the future at creation  |
| now         | Instant | Current time (passed by command handler via Clock) | Required, used for future-date validation    |
| movie       | Movie   | The movie being shown (embedded value object)     | Required, immutable                          |
| hall        | Hall    | The hall where the show takes place (embedded VO) | Required, immutable                          |

**Identity**: `showId` (not the JPA surrogate `id`)
**Lifecycle**: Created once, never modified (immutable after construction)
**Events raised**: `ShowScheduled` in constructor
**Clock discipline**: The `now` parameter is not persisted — it is a transient construction-time input used solely for the `Contract.check(scheduledAt.isAfter(now))` invariant. The application-layer command handler obtains it from the injected `Clock` bean (`clock.instant()`). This satisfies constitution v1.6.0 Clock injection rule while keeping the domain layer free of `Clock` dependencies.

**JPA mapping** (ORM XML):

- Table: `show`
- Sequence: `show_s` (START 1, INCREMENT 50)
- Columns: `id_`, `version_`, `show_id`, `scheduled_at`, `movie_id`, `movie_title`, `movie_runtime_minutes`, `hall_id`,
  `hall_name`, `hall_seat_count`
- All identifier columns marked `insertable="true" updatable="false"`

## Value Objects

### ShowId

| Field | Type   | Constraints                |
|-------|--------|----------------------------|
| value | String | Required, `S0[0-9A-F]{16}` |

### Movie

| Field          | Type    | Constraints         |
|----------------|---------|---------------------|
| movieId        | MovieId | Required            |
| title          | String  | Required, non-blank |
| runtimeMinutes | int     | Required, > 0       |

**Note**: This is the scheduling context's own `Movie` value object, distinct from booking's `Movie`. It includes
`runtimeMinutes` for overlap calculation.

### MovieId

| Field | Type   | Constraints                |
|-------|--------|----------------------------|
| value | String | Required, `M0[0-9A-F]{16}` |

### Hall

| Field     | Type   | Constraints         |
|-----------|--------|---------------------|
| hallId    | HallId | Required            |
| name      | String | Required, non-blank |
| seatCount | int    | Required, > 0       |

**Note**: The scheduling context's `Hall` is simpler than booking's — it holds `seatCount` instead of `SeatLayout` since
scheduling does not manage individual seats.

### HallId

| Field | Type   | Constraints                |
|-------|--------|----------------------------|
| value | String | Required, `H0[0-9A-F]{16}` |

## Events

### ShowEvent (Abstract Base)

Base event class for the Show aggregate, implementing seedwork `Event`.

| Field  | Type   | Constraints     |
|--------|--------|-----------------|
| showId | ShowId | Required, final |

### ShowScheduled (Domain Event)

Published in the Show constructor when a show is created.

| Field       | Type    | Constraints                 |
|-------------|---------|-----------------------------|
| showId      | ShowId  | Required, final (inherited) |
| movieId     | MovieId | Required, final             |
| hallId      | HallId  | Required, final             |
| scheduledAt | Instant | Required, final             |

## Domain Services

### MovieService (Interface)

Provides access to movie data from the future movies bounded context.

| Method                       | Returns       | Description               |
|------------------------------|---------------|---------------------------|
| `movieFrom(MovieId movieId)` | `Movie`       | Look up a movie by ID     |
| `listMovies()`               | `List<Movie>` | List all available movies |

### HallService (Interface)

Provides access to hall data from the future halls bounded context.

| Method                    | Returns      | Description              |
|---------------------------|--------------|--------------------------|
| `hallFrom(HallId hallId)` | `Hall`       | Look up a hall by ID     |
| `listHalls()`             | `List<Hall>` | List all available halls |

### ShowSchedulingPolicy (Interface)

Enforces the hall-time overlap invariant.

| Method                                                       | Returns | Description                                |
|--------------------------------------------------------------|---------|--------------------------------------------|
| `ensureNoOverlap(HallId hallId, Instant start, Instant end)` | `void`  | Throws `ShowException` if overlap detected |

## Exceptions

### ShowException

| Problem Constant        | Factory Method   | HTTP Status | Description                          |
|-------------------------|------------------|-------------|--------------------------------------|
| `NOT_FOUND_PROBLEM`     | `notFound()`     | 404         | Show not found                       |
| `OVERLAP_PROBLEM`       | `overlap()`      | 409         | Hall already has a show at this time |
| `PAST_SCHEDULE_PROBLEM` | `pastSchedule()` | 409         | Cannot schedule a show in the past   |

## Relationships

```
Show (aggregate root)
├── showId: ShowId (embedded VO)
├── movie: Movie (embedded VO)
│   └── movieId: MovieId (embedded VO)
├── hall: Hall (embedded VO)
│   └── hallId: HallId (embedded VO)
└── raises: ShowScheduled (domain event)
        ├── showId: ShowId
        ├── movieId: MovieId
        ├── hallId: HallId
        └── scheduledAt: Instant
```

## Database Schema (Flyway Migration)

```sql
-- V1_0__show.sql
CREATE SEQUENCE show_s START WITH 1 INCREMENT BY 50;

CREATE TABLE show
(
  id_                   BIGINT      NOT NULL PRIMARY KEY,
  version_              BIGINT      NOT NULL,
  show_id               VARCHAR(18) NOT NULL,
  scheduled_at          TIMESTAMP   NOT NULL,
  movie_id              VARCHAR(18) NOT NULL,
  movie_title           TEXT        NOT NULL,
  movie_runtime_minutes INTEGER     NOT NULL,
  hall_id               VARCHAR(18) NOT NULL,
  hall_name             TEXT        NOT NULL,
  hall_seat_count       INTEGER     NOT NULL,

  CONSTRAINT uq_show_show_id UNIQUE (show_id)
);
```

## Query Projections

### ShowDetailView

| Field               | Source Column         |
|---------------------|-----------------------|
| showId              | show_id               |
| scheduledAt         | scheduled_at          |
| movieId             | movie_id              |
| movieTitle          | movie_title           |
| movieRuntimeMinutes | movie_runtime_minutes |
| hallId              | hall_id               |
| hallName            | hall_name             |
| hallSeatCount       | hall_seat_count       |

### ShowSummaryView

| Field       | Source Column |
|-------------|---------------|
| showId      | show_id       |
| scheduledAt | scheduled_at  |
| movieTitle  | movie_title   |
| hallName    | hall_name     |
