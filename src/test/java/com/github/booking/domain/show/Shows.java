package com.github.booking.domain.show;

import com.github.booking.domain.booking.Bookings;
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
    final var booking = Bookings.newInitiatedBooking(showId, bookingId);

    show.bookSeats(booking);
    show.releaseEvents(Consumers.empty());

    return show;
  }

  public static Show newShowWithReservedSeats(final String showId, final String bookingId, final String... seatNumbers) {
    final var show = newShow(showId);
    final var booking = Bookings.newInitiatedBooking(showId, bookingId);

    for (final var seatNumber : seatNumbers) {
      show.reserveSeat(booking, new SeatNumber(seatNumber));
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
