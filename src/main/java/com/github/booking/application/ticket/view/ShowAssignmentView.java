package com.github.booking.application.ticket.view;

import java.time.Instant;

public record ShowAssignmentView(String movieTitle,
                                 String hallName,
                                 Instant scheduledAt,
                                 String seatNumber) {

  public static ShowAssignmentView of(final String movieTitle,
                                      final String hallName,
                                      final Instant scheduledAt,
                                      final String seatNumber) {
    return new ShowAssignmentView(movieTitle, hallName, scheduledAt, seatNumber);
  }
}
