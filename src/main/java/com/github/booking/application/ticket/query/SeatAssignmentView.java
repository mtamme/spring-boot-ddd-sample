package com.github.booking.application.ticket.query;

import java.time.Instant;

public record SeatAssignmentView(String movieTitle,
                                 String hallName,
                                 Instant scheduledAt,
                                 String seatNumber) {

  public static SeatAssignmentView of(final String movieTitle,
                                      final String hallName,
                                      final Instant scheduledAt,
                                      final String seatNumber) {
    return new SeatAssignmentView(movieTitle, hallName, scheduledAt, seatNumber);
  }
}
