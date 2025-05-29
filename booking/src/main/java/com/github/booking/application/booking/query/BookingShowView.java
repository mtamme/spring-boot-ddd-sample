package com.github.booking.application.booking.query;

import java.time.Instant;

public record BookingShowView(String showId, Instant scheduledAt) {

  public static BookingShowView of(final String showId, final Instant scheduledAt) {
    if (showId == null) {
      return null;
    }

    return new BookingShowView(showId, scheduledAt);
  }
}
