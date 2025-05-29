package com.github.booking.domain.booking;

import java.util.Optional;

public interface BookingRepository {

  BookingId nextBookingId();

  Optional<Booking> findByBookingId(BookingId bookingId);

  void save(Booking booking);
}
