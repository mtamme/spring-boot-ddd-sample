package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.BookingQueryHandler;
import com.github.booking.application.booking.view.BookingDetailView;
import com.github.booking.application.booking.view.BookingSummaryView;
import com.github.booking.domain.booking.BookingNotFoundException;
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
  public BookingDetailView getBooking(final String bookingId) {
    return repository.find(bookingId)
      .orElseThrow(BookingNotFoundException::new);
  }

  @Override
  public List<BookingSummaryView> listBookings(final long offset, final int limit) {
    return repository.findAll(offset, limit);
  }
}
