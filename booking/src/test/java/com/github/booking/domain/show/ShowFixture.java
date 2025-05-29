package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.HallFixture;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.MovieFixture;
import com.github.seedwork.core.util.Consumers;

import java.time.Instant;

public final class ShowFixture {

  private ShowFixture() {
  }

  public static Show newShowWithBookedSeats(final String showId, final String bookingId, final String... seatNumbers) {
    final var show = newShowWithReservedSeats(showId, bookingId, seatNumbers);

    show.bookSeats(new BookingId(bookingId));
    show.dispatchEvents(Consumers.noop());

    return show;
  }

  public static Show newShowWithReservedSeats(final String showId, final String bookingId, final String... seatNumbers) {
    final var show = newShow(showId);

    for (final var seatNumber : seatNumbers) {
      show.reserveSeat(new BookingId(bookingId), new SeatNumber(seatNumber));
    }
    show.dispatchEvents(Consumers.noop());

    return show;
  }

  public static Show newShow(final String showId) {
    final var show = new Show(
      new ShowId(showId),
      Instant.EPOCH,
      MovieFixture.newMovie("M00000000000000000"),
      HallFixture.newHall("H00000000000000000"));

    show.dispatchEvents(Consumers.noop());

    return show;
  }
}
