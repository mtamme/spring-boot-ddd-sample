package com.github.booking.application.booking.query;

import java.time.Instant;

public record BookingSummaryView(String bookingId, String status, BookingShowView show) {

  public BookingSummaryView(final String bookingId,
                            final String status,
                            final String showId,
                            final Instant showScheduledAt) {
    this(bookingId, status, BookingShowView.of(showId, showScheduledAt));
  }
}
