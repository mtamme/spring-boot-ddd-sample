package com.github.booking.application.ticket.query;

public record TicketBookingView(String bookingId, String status) {

  public static TicketBookingView of(final String bookingId, final String status) {
    if (bookingId == null) {
      return null;
    }

    return new TicketBookingView(bookingId, status);
  }
}
