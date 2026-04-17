package com.github.scheduling.application.show.command;

import java.time.Instant;

public record ScheduleShowCommand(Instant scheduledAt,
                                  String movieId,
                                  String hallId) {
}
