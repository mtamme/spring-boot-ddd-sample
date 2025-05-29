package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.Halls;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.Movies;
import com.github.seedwork.core.util.Consumers;

import java.time.Instant;

public final class Shows {

  private Shows() {
  }

  public static Show newShowWithBookedSeats(final String showId, final String bookingId, final String... seatNumbers) {
    final var show = newShowWithReservedSeats(showId, bookingId, seatNumbers);

    show.bookSeats(new BookingId(bookingId));
    show.releaseEvents(Consumers.empty());

    return show;
  }

  public static Show newShowWithReservedSeats(final String showId, final String bookingId, final String... seatNumbers) {
    final var show = newShow(showId);

    for (final var seatNumber : seatNumbers) {
      show.reserveSeat(new BookingId(bookingId), new SeatNumber(seatNumber));
    }
    show.releaseEvents(Consumers.empty());

    return show;
  }

  public static Show newShow(final String showId) {
    final var show = new Show(
      new ShowId(showId),
      Instant.EPOCH,
      Movies.newMovie("M0000000000"),
      Halls.newHall("H0000000000"));

    show.releaseEvents(Consumers.empty());

    return show;
  }
}
