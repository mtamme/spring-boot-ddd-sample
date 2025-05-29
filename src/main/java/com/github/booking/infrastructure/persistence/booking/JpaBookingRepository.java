package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.query.BookingDetailView;
import com.github.booking.application.booking.query.BookingSummaryView;
import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.seedwork.core.util.RandomSupport;
import com.github.seedwork.infrastructure.persistence.JpaAggregateSaver;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaBookingRepository extends JpaAggregateSaver<Booking>, Repository<Booking, Long>, BookingRepository {

  @NativeQuery(name = "BookingDetailView.getBooking")
  Optional<BookingDetailView> getBooking(@Param("booking_id") String bookingId);

  @NativeQuery(name = "BookingSummaryView.listBookings")
  List<BookingSummaryView> listBookings(@Param("offset") long offset,
                                        @Param("limit") int limit);

  @Override
  default BookingId nextBookingId() {
    return new BookingId("B0%016X".formatted(RandomSupport.nextLong()));
  }

  @Override
  default void save(final Booking booking) {
    saveAndPublishEvents(booking.bookingId().value(), booking);
  }
}
