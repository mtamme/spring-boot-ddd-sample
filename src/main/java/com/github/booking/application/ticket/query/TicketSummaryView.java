package com.github.booking.application.ticket.query;

import java.time.Instant;

public record TicketSummaryView(String ticketId,
                                String status,
                                TicketBookingView booking,
                                SeatAssignmentView seatAssignment) {

  public TicketSummaryView(final String ticketId,
                           final String status,
                           final String bookingId,
                           final String bookingStatus,
                           final String seatAssignmentMovieTitle,
                           final String seatAssignmentHallName,
                           final Instant seatAssignmentScheduledAt,
                           final String seatAssignmentSeatNumber) {
    this(
      ticketId,
      status,
      TicketBookingView.of(bookingId, bookingStatus),
      SeatAssignmentView.of(seatAssignmentMovieTitle, seatAssignmentHallName, seatAssignmentScheduledAt, seatAssignmentSeatNumber));
  }
}
