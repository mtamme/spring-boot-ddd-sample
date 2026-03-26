package com.github.scheduling.infrastructure.agent;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.*;
import com.github.scheduling.application.hall.HallQueryHandler;
import com.github.scheduling.application.hall.query.ListHallsQuery;
import com.github.scheduling.application.movie.MovieQueryHandler;
import com.github.scheduling.application.movie.query.ListMoviesQuery;
import com.github.scheduling.application.show.ShowCommandHandler;
import com.github.scheduling.application.show.ShowQueryHandler;
import com.github.scheduling.application.show.command.ScheduleShowCommand;
import com.github.scheduling.application.show.query.ListShowsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SchedulingAgent {

  private static final Logger log = LoggerFactory.getLogger(SchedulingAgent.class);
  private static final String SYSTEM_PROMPT = """
    You are a cinema scheduling assistant. You help administrators schedule movie shows in cinema halls.
    Use the available tools to look up movies and halls, and to schedule shows.
    Always look up available movies and halls before scheduling a show.
    When scheduling, use the exact movie_id and hall_id from the lookup results.
    Provide clear confirmations after scheduling a show.
    """;

  private final AnthropicClient client;
  private final ShowCommandHandler showCommandHandler;
  private final ShowQueryHandler showQueryHandler;
  private final HallQueryHandler hallQueryHandler;
  private final MovieQueryHandler movieQueryHandler;

  public SchedulingAgent(@Value("${anthropic.api-key:}") final String apiKey,
                         final ShowCommandHandler showCommandHandler,
                         final ShowQueryHandler showQueryHandler,
                         final HallQueryHandler hallQueryHandler,
                         final MovieQueryHandler movieQueryHandler) {
    this.client = apiKey.isBlank()
      ? null
      : AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    this.showCommandHandler = showCommandHandler;
    this.showQueryHandler = showQueryHandler;
    this.hallQueryHandler = hallQueryHandler;
    this.movieQueryHandler = movieQueryHandler;
  }

  public String processMessage(final String message) {
    if (client == null) {
      return "AI agent is not configured. Please set the ANTHROPIC_API_KEY environment variable.";
    }

    final var tools = buildTools();
    final var messages = new ArrayList<MessageParam>();

    messages.add(MessageParam.builder()
      .role(MessageParam.Role.USER)
      .content(message)
      .build());

    for (int i = 0; i < 10; i++) {
      final var params = MessageCreateParams.builder()
        .model(Model.CLAUDE_SONNET_4_5)
        .maxTokens(1024L)
        .system(SYSTEM_PROMPT)
        .messages(messages)
        .tools(tools)
        .build();

      final var response = client.messages().create(params);

      if (response.stopReason().orElse(null) == StopReason.END_TURN) {
        return extractTextContent(response);
      }

      if (response.stopReason().orElse(null) == StopReason.TOOL_USE) {
        messages.add(MessageParam.builder()
          .role(MessageParam.Role.ASSISTANT)
          .contentOfBlockParams(toAssistantBlocks(response))
          .build());
        final var toolResults = processToolCalls(response);

        messages.add(MessageParam.builder()
          .role(MessageParam.Role.USER)
          .contentOfBlockParams(toolResults)
          .build());
      }
    }

    return "The agent was unable to complete the request within the allowed number of steps.";
  }

  private List<ToolUnion> buildTools() {
    return List.of(
      ToolUnion.ofTool(buildScheduleShowTool()),
      ToolUnion.ofTool(buildListHallsTool()),
      ToolUnion.ofTool(buildListMoviesTool()),
      ToolUnion.ofTool(buildListShowsTool()));
  }

  Tool buildScheduleShowTool() {
    return Tool.builder()
      .name("schedule_show")
      .description("Schedule a new cinema show at a specific date/time in a specific hall for a specific movie.")
      .inputSchema(Tool.InputSchema.builder()
        .properties(JsonValue.from(Map.of(
          "movie_id", Map.of("type", "string", "description", "The movie ID (format: M0 followed by 16 hex digits)"),
          "hall_id", Map.of("type", "string", "description", "The hall ID (format: H0 followed by 16 hex digits)"),
          "scheduled_at", Map.of("type", "string", "format", "date-time", "description", "The date and time when the show starts (ISO 8601 format, must be in the future)")
        )))
        .putAdditionalProperty("required", JsonValue.from(List.of("movie_id", "hall_id", "scheduled_at")))
        .build())
      .build();
  }

  Tool buildListHallsTool() {
    return Tool.builder()
      .name("list_halls")
      .description("List all available cinema halls with their details.")
      .inputSchema(Tool.InputSchema.builder()
        .properties(JsonValue.from(Map.of()))
        .build())
      .build();
  }

  Tool buildListMoviesTool() {
    return Tool.builder()
      .name("list_movies")
      .description("List all available movies with their details.")
      .inputSchema(Tool.InputSchema.builder()
        .properties(JsonValue.from(Map.of()))
        .build())
      .build();
  }

  Tool buildListShowsTool() {
    return Tool.builder()
      .name("list_shows")
      .description("List previously scheduled shows with pagination.")
      .inputSchema(Tool.InputSchema.builder()
        .properties(JsonValue.from(Map.of(
          "offset", Map.of("type", "integer", "description", "Number of shows to skip (default: 0)"),
          "limit", Map.of("type", "integer", "description", "Maximum number of shows to return (default: 10)")
        )))
        .build())
      .build();
  }

  private List<ContentBlockParam> processToolCalls(final Message response) {
    final var results = new ArrayList<ContentBlockParam>();

    for (final var block : response.content()) {
      if (block.isToolUse()) {
        final var toolUse = block.asToolUse();
        final var result = executeToolCall(toolUse.name(), toolUse._input());

        results.add(ContentBlockParam.ofToolResult(
          ToolResultBlockParam.builder()
            .toolUseId(toolUse.id())
            .content(result)
            .build()));
      }
    }

    return results;
  }

  String executeToolCall(final String toolName, final JsonValue input) {
    try {
      return switch (toolName) {
        case "schedule_show" -> executeScheduleShow(input);
        case "list_halls" -> executeListHalls();
        case "list_movies" -> executeListMovies();
        case "list_shows" -> executeListShows(input);
        default -> "Unknown tool: " + toolName;
      };
    } catch (final Exception e) {
      log.error("Tool call failed: {} - {}", toolName, e.getMessage());
      return "Error: " + e.getMessage();
    }
  }

  @SuppressWarnings("unchecked")
  private String executeScheduleShow(final JsonValue input) {
    final var map = (Map<String, JsonValue>) input.asObject().orElseThrow();
    final var movieId = (String) map.get("movie_id").asString().orElseThrow();
    final var hallId = (String) map.get("hall_id").asString().orElseThrow();
    final var scheduledAt = Instant.parse((String) map.get("scheduled_at").asString().orElseThrow());

    final var result = showCommandHandler.scheduleShow(
      new ScheduleShowCommand(scheduledAt, movieId, hallId));

    return "Show scheduled successfully with ID: " + result.showId();
  }

  private String executeListHalls() {
    final var halls = hallQueryHandler.listHalls(new ListHallsQuery());
    final var sb = new StringBuilder("[");

    for (int i = 0; i < halls.size(); i++) {
      final var hall = halls.get(i);

      if (i > 0) {
        sb.append(",");
      }
      sb.append("{\"hallId\":\"%s\",\"name\":\"%s\",\"seatCount\":%d}"
        .formatted(hall.hallId(), hall.name(), hall.seatCount()));
    }
    sb.append("]");

    return sb.toString();
  }

  private String executeListMovies() {
    final var movies = movieQueryHandler.listMovies(new ListMoviesQuery());
    final var sb = new StringBuilder("[");

    for (int i = 0; i < movies.size(); i++) {
      final var movie = movies.get(i);

      if (i > 0) {
        sb.append(",");
      }
      sb.append("{\"movieId\":\"%s\",\"title\":\"%s\",\"runtimeMinutes\":%d}"
        .formatted(movie.movieId(), movie.title(), movie.runtimeMinutes()));
    }
    sb.append("]");

    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  private String executeListShows(final JsonValue input) {
    final var map = (Map<String, JsonValue>) input.asObject().orElseThrow();

    final long offset = map.containsKey("offset")
      ? ((Number) map.get("offset").asNumber().orElseThrow()).longValue()
      : 0L;
    final int limit = map.containsKey("limit")
      ? ((Number) map.get("limit").asNumber().orElseThrow()).intValue()
      : 10;

    final var shows = showQueryHandler.listShows(new ListShowsQuery(offset, limit));
    final var sb = new StringBuilder("[");

    for (int i = 0; i < shows.size(); i++) {
      final var show = shows.get(i);

      if (i > 0) {
        sb.append(",");
      }
      sb.append("{\"showId\":\"%s\",\"scheduledAt\":\"%s\",\"movieTitle\":\"%s\",\"hallName\":\"%s\"}"
        .formatted(show.showId(), show.scheduledAt(), show.movieTitle(), show.hallName()));
    }
    sb.append("]");

    return sb.toString();
  }

  private String extractTextContent(final Message message) {
    final var sb = new StringBuilder();

    for (final var block : message.content()) {
      if (block.isText()) {
        sb.append(block.asText().text());
      }
    }

    return sb.toString();
  }

  private List<ContentBlockParam> toAssistantBlocks(final Message message) {
    final var blocks = new ArrayList<ContentBlockParam>();

    for (final var block : message.content()) {
      if (block.isText()) {
        blocks.add(ContentBlockParam.ofText(
          TextBlockParam.builder().text(block.asText().text()).build()));
      } else if (block.isToolUse()) {
        final var toolUse = block.asToolUse();

        blocks.add(ContentBlockParam.ofToolUse(
          ToolUseBlockParam.builder()
            .id(toolUse.id())
            .name(toolUse.name())
            .input(toolUse._input())
            .build()));
      }
    }

    return blocks;
  }
}
