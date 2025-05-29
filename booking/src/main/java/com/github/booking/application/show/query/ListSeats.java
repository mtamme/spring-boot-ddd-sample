package com.github.booking.application.show.query;

import com.github.seedwork.application.QueryHandler;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ListSeats extends QueryHandler<ListSeats.Query, List<ListSeats.Seat>> {

  record Query(String showId) {
  }

  record Seat(String seatNumber, String status, Booking booking) {

    public Seat(final String seatNumber,
                final String status,
                final String bookingId,
                final String bookingStatus) {
      this(seatNumber, status, new Booking(bookingId, bookingStatus));
    }
  }

  record Booking(String bookingId, String status) {
  }
}