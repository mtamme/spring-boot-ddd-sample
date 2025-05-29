package com.github.booking.domain.booking;

import com.github.booking.domain.show.Shows;
import com.github.seedwork.core.util.Consumers;

public final class Bookings {

  private Bookings() {
  }

  public static Booking newConfirmedBooking(final String showId, final String bookingId) {
    final var booking = newInitiatedBooking(showId, bookingId);

    booking.confirm();
    booking.releaseEvents(Consumers.empty());

    return booking;
  }

  public static Booking newCancelledBooking(final String showId, final String bookingId) {
    final var booking = newInitiatedBooking(showId, bookingId);

    booking.cancel();
    booking.releaseEvents(Consumers.empty());

    return booking;
  }

  public static Booking newInitiatedBooking(final String showId, final String bookingId) {
    final var show = Shows.newShow(showId);
    final var booking = show.initiateBooking(new BookingId(bookingId));

    booking.releaseEvents(Consumers.empty());

    return booking;
  }
}
