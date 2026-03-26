package com.github.scheduling.application.show.query;

import java.time.Instant;

public record ShowDetailView(
  String showId,
  Instant scheduledAt,
  String movieId,
  String movieTitle,
  int movieRuntimeMinutes,
  String hallId,
  String hallName,
  int hallSeatCount) {
}
