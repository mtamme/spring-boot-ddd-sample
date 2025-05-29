package com.github.booking.application.show.command;

import java.time.Instant;

public record ScheduleShowCommand(String showId,
                                  Instant scheduledAt,
                                  String movieId,
                                  String hallId) {
}
