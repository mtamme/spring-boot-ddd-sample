package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;

public class BookingConfirmed extends BookingEvent {

  public BookingConfirmed(final ShowId showId, final BookingId bookingId) {
    super(showId, bookingId);
  }
}
