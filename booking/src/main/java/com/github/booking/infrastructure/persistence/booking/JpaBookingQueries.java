package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.query.GetBooking;
import com.github.booking.application.booking.query.ListBookings;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaBookingQueries {

  @NativeQuery(name = "Booking.getBooking")
  Optional<GetBooking.Booking> getBooking(@Param("booking_id") String bookingId);

  @NativeQuery(name = "Booking.listBookings")
  List<ListBookings.Booking> listBookings(@Param("offset") long offset,
                                          @Param("limit") int limit);
}
