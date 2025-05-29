package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.query.GetBooking;
import com.github.booking.domain.booking.BookingException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class JpaGetBooking implements GetBooking {

  private final JpaBookingQueries queries;

  public JpaGetBooking(final JpaBookingQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public Booking handle(final Query query) {
    return queries.getBooking(query.bookingId())
      .orElseThrow(BookingException::notFound);
  }
}
