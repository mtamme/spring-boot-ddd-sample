package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.seedwork.core.util.Randoms;
import com.github.seedwork.infrastructure.persistence.repository.JpaAggregateRootSupport;
import org.springframework.data.repository.Repository;

public interface JpaBookingRepository extends JpaAggregateRootSupport, JpaBookingQueries, BookingRepository, Repository<Booking, Long> {

  @Override
  default BookingId nextBookingId() {
    return new BookingId("B0%016X".formatted(Randoms.nextLong()));
  }

  @Override
  default void save(final Booking booking) {
    saveAndPublishEvents(booking.bookingId().value(), booking);
  }
}
