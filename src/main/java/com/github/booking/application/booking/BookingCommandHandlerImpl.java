package com.github.booking.application.booking;

import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingResult;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import com.github.booking.domain.booking.BookingException;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BookingCommandHandlerImpl implements BookingCommandHandler {

  private final BookingRepository bookingRepository;
  private final ShowRepository showRepository;

  public BookingCommandHandlerImpl(final BookingRepository bookingRepository, final ShowRepository showRepository) {
    this.bookingRepository = Objects.requireNonNull(bookingRepository);
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @Override
  public InitiateBookingResult initiateBooking(final InitiateBookingCommand command) {
    final var show = showRepository.findByShowId(new ShowId(command.showId()))
      .orElseThrow(ShowException::notFound);
    final var booking = show.initiateBooking(bookingRepository.nextBookingId());

    bookingRepository.save(booking);

    return new InitiateBookingResult(booking.bookingId()
      .value());
  }

  @Override
  public void reserveSeat(final ReserveSeatCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingException::notFound);
    final var show = showRepository.findByShowId(booking.showId())
      .orElseThrow(ShowException::notFound);

    show.reserveSeat(booking, new SeatNumber(command.seatNumber()));
    showRepository.save(show);
  }

  @Override
  public void releaseSeat(final ReleaseSeatCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingException::notFound);
    final var show = showRepository.findByShowId(booking.showId())
      .orElseThrow(ShowException::notFound);

    show.releaseSeat(booking, new SeatNumber(command.seatNumber()));
    showRepository.save(show);
  }

  @Override
  public void confirmBooking(final ConfirmBookingCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingException::notFound);

    booking.confirm();
    bookingRepository.save(booking);
  }

  @Override
  public void cancelBooking(final CancelBookingCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingException::notFound);

    booking.cancel();
    bookingRepository.save(booking);
  }
}
