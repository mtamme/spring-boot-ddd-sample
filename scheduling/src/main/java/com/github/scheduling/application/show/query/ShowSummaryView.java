package com.github.scheduling.application.show.query;

import java.time.Instant;

public record ShowSummaryView(
  String showId,
  Instant scheduledAt,
  String movieTitle,
  String hallName) {
}
