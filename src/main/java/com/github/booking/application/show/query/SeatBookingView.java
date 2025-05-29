package com.github.booking.application.show.query;

public record SeatBookingView(String bookingId, String status) {

  public static SeatBookingView of(final String bookingId, final String status) {
    if (bookingId == null) {
      return null;
    }

    return new SeatBookingView(bookingId, status);
  }
}
