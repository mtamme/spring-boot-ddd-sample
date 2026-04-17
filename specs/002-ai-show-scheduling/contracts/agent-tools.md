# AI Agent Tool Definitions

**Branch**: `002-ai-show-scheduling` | **Date**: 2026-03-26

These are the tools exposed to the Claude LLM by the AI scheduling agent. Each tool maps directly to an
application-layer command or query handler.

## Tool: `schedule_show`

**Maps to**: `ShowCommandHandler.scheduleShow(ScheduleShowCommand)`

**Description**: Schedule a new cinema show at a specific date/time in a specific hall for a specific movie.

**Input Schema**:

```json
{
  "type": "object",
  "properties": {
    "movie_id": {
      "type": "string",
      "description": "The movie ID (format: M0 followed by 16 hex digits)"
    },
    "hall_id": {
      "type": "string",
      "description": "The hall ID (format: H0 followed by 16 hex digits)"
    },
    "scheduled_at": {
      "type": "string",
      "format": "date-time",
      "description": "The date and time when the show starts (ISO 8601 format, must be in the future)"
    }
  },
  "required": ["movie_id", "hall_id", "scheduled_at"]
}
```

**Returns**: Confirmation message with the new show ID, or error if scheduling failed (past date, overlap, invalid
hall/movie).

## Tool: `list_halls`

**Maps to**: `HallQueryHandler.listHalls(ListHallsQuery)`

**Description**: List all available cinema halls with their names and seating capacity.

**Input Schema**:

```json
{
  "type": "object",
  "properties": {}
}
```

**Returns**: JSON array of halls with `hallId`, `name`, and `seatCount`.

## Tool: `list_movies`

**Maps to**: `MovieQueryHandler.listMovies(ListMoviesQuery)`

**Description**: List all available movies with their titles and runtime.

**Input Schema**:

```json
{
  "type": "object",
  "properties": {}
}
```

**Returns**: JSON array of movies with `movieId`, `title`, and `runtimeMinutes`.

## Tool: `list_shows`

**Maps to**: `ShowQueryHandler.listShows(ListShowsQuery)`

**Description**: List all scheduled shows with their movie, hall, and date/time.

**Input Schema**:

```json
{
  "type": "object",
  "properties": {
    "offset": {
      "type": "integer",
      "description": "Number of records to skip (default: 0)",
      "default": 0
    },
    "limit": {
      "type": "integer",
      "description": "Maximum number of records to return (default: 10, max: 100)",
      "default": 10
    }
  }
}
```

**Returns**: JSON array of shows with `showId`, `scheduledAt`, `movieTitle`, and `hallName`.
