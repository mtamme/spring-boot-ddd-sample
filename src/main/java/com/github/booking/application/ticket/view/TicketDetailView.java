package com.github.booking.application.ticket.view;

import java.time.Instant;

public record TicketDetailView(String ticketId,
                               String status,
                               TicketBookingView booking,
                               ShowAssignmentView showAssignment) {

  public TicketDetailView(final String ticketId,
                          final String status,
                          final String bookingId,
                          final String bookingStatus,
                          final String showAssignmentMovieTitle,
                          final String showAssignmentHallName,
                          final Instant showAssignmentScheduledAt,
                          final String showAssignmentSeatNumber) {
    this(
      ticketId,
      status,
      TicketBookingView.of(bookingId, bookingStatus),
      ShowAssignmentView.of(showAssignmentMovieTitle, showAssignmentHallName, showAssignmentScheduledAt, showAssignmentSeatNumber));
  }
}
