package com.github.booking.application.booking;

import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingNotFoundException;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowNotFoundException;
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
  public String initiateBooking(final InitiateBookingCommand command) {
    final var show = showRepository.findByShowId(new ShowId(command.showId()))
      .orElseThrow(ShowNotFoundException::new);
    final var bookingId = bookingRepository.nextBookingId();

    bookingRepository.save(new Booking(show, bookingId));

    return bookingId.value();
  }

  @Override
  public void reserveSeat(final ReserveSeatCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingNotFoundException::new);
    final var show = showRepository.findByShowId(booking.showId())
      .orElseThrow(ShowNotFoundException::new);

    show.reserveSeat(booking, new SeatNumber(command.seatNumber()));
    showRepository.save(show);
  }

  @Override
  public void releaseSeat(final ReleaseSeatCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingNotFoundException::new);
    final var show = showRepository.findByShowId(booking.showId())
      .orElseThrow(ShowNotFoundException::new);

    show.releaseSeat(booking, new SeatNumber(command.seatNumber()));
    showRepository.save(show);
  }

  @Override
  public void confirmBooking(final ConfirmBookingCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingNotFoundException::new);

    booking.confirm();
    bookingRepository.save(booking);
  }

  @Override
  public void cancelBooking(final CancelBookingCommand command) {
    final var booking = bookingRepository.findByBookingId(new BookingId(command.bookingId()))
      .orElseThrow(BookingNotFoundException::new);

    booking.cancel();
    bookingRepository.save(booking);
  }
}
