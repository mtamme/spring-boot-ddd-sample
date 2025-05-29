package com.github.booking.application.booking.command;

import com.github.booking.domain.booking.BookingException;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class CancelBookingImpl implements CancelBooking {

  private final BookingRepository bookingRepository;

  public CancelBookingImpl(final BookingRepository bookingRepository) {
    this.bookingRepository = Objects.requireNonNull(bookingRepository);
  }

  @Override
  public void handle(final Command command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingException::notFound);

    booking.cancel();
    bookingRepository.save(booking);
  }
}
