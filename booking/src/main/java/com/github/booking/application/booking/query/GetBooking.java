package com.github.booking.application.booking.query;

import com.github.seedwork.application.QueryHandler;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Transactional(readOnly = true)
public interface GetBooking extends QueryHandler<GetBooking.Query, GetBooking.Booking> {

  record Query(String bookingId) {
  }

  record Booking(String bookingId, String status, Show show) {

    public Booking(final String bookingId,
                   final String status,
                   final String showId,
                   final Instant showScheduledAt) {
      this(bookingId, status, new Show(showId, showScheduledAt));
    }
  }

  record Show(String showId, Instant scheduledAt) {
  }
}
