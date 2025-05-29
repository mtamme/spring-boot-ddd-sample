package com.github.booking.application.show.query;

public record SeatView(String seatNumber, String status, SeatBookingView booking) {

  public SeatView(final String seatNumber,
                  final String status,
                  final String bookingId,
                  final String bookingStatus) {
    this(seatNumber, status, SeatBookingView.of(bookingId, bookingStatus));
  }
}
