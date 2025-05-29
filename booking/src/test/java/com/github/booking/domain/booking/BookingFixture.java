package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowFixture;
import com.github.seedwork.core.util.Consumers;

public final class BookingFixture {

  private BookingFixture() {
  }

  public static Booking newConfirmedBooking(final String showId, final String bookingId) {
    final var booking = newInitiatedBooking(showId, bookingId);

    booking.confirm();
    booking.dispatchEvents(Consumers.noop());

    return booking;
  }

  public static Booking newCancelledBooking(final String showId, final String bookingId) {
    final var booking = newInitiatedBooking(showId, bookingId);

    booking.cancel();
    booking.dispatchEvents(Consumers.noop());

    return booking;
  }

  public static Booking newInitiatedBooking(final String showId, final String bookingId) {
    final var show = ShowFixture.newShow(showId);
    final var booking = show.initiateBooking(new BookingId(bookingId));

    booking.dispatchEvents(Consumers.noop());

    return booking;
  }
}
