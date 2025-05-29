package com.github.booking.application.booking.command;

import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class InitiateBookingImpl implements InitiateBooking {

  private final BookingRepository bookingRepository;
  private final ShowRepository showRepository;

  public InitiateBookingImpl(final BookingRepository bookingRepository, final ShowRepository showRepository) {
    this.bookingRepository = Objects.requireNonNull(bookingRepository);
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @Override
  public Booking handle(final Command command) {
    final var show = showRepository.findByShowId(new ShowId(command.showId()))
      .orElseThrow(ShowException::notFound);
    final var booking = show.initiateBooking(bookingRepository.nextBookingId());

    bookingRepository.save(booking);

    return new Booking(booking.bookingId()
      .value());
  }
}
