package com.github.booking.application.booking.query;

import com.github.seedwork.application.QueryHandler;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Transactional(readOnly = true)
public interface ListBookings extends QueryHandler<ListBookings.Query, List<ListBookings.Booking>> {

  record Query(long offset, int limit) {
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
