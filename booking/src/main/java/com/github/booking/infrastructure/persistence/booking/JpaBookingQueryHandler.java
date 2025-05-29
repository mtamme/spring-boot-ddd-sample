package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.BookingQueryHandler;
import com.github.booking.application.booking.query.BookingDetailView;
import com.github.booking.application.booking.query.BookingSummaryView;
import com.github.booking.application.booking.query.GetBookingQuery;
import com.github.booking.application.booking.query.ListBookingsQuery;
import com.github.booking.domain.booking.BookingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JpaBookingQueryHandler implements BookingQueryHandler {

  private final JpaBookingRepository repository;

  public JpaBookingQueryHandler(final JpaBookingRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public BookingDetailView getBooking(final GetBookingQuery query) {
    return repository.getBooking(query.bookingId())
      .orElseThrow(BookingException::notFound);
  }

  @Override
  public List<BookingSummaryView> listBookings(final ListBookingsQuery query) {
    return repository.listBookings(query.offset(), query.limit());
  }
}
