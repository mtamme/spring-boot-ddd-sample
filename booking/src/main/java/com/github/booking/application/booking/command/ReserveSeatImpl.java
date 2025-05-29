package com.github.booking.application.booking.command;

import com.github.booking.domain.booking.BookingException;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class ReserveSeatImpl implements ReserveSeat {

  private final BookingRepository bookingRepository;
  private final ShowRepository showRepository;

  public ReserveSeatImpl(final BookingRepository bookingRepository, final ShowRepository showRepository) {
    this.bookingRepository = Objects.requireNonNull(bookingRepository);
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @Override
  public void handle(final Command command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingException::notFound);

    if (!booking.isInitiated()) {
      throw BookingException.notInitiated();
    }
    final var show = showRepository.findByShowId(booking.showId())
      .orElseThrow(ShowException::notFound);

    show.reserveSeat(booking.bookingId(), new SeatNumber(command.seatNumber()));
    showRepository.save(show);
  }
}
