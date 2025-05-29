package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.query.ListBookings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
class JpaListBookings implements ListBookings {

  private final JpaBookingQueries queries;

  public JpaListBookings(final JpaBookingQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public List<Booking> handle(final Query query) {
    return queries.listBookings(query.offset(), query.limit());
  }
}
