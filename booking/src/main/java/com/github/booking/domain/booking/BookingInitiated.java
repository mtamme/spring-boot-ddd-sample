package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;

public class BookingInitiated extends BookingEvent {

  public BookingInitiated(final ShowId showId, final BookingId bookingId) {
    super(showId, bookingId);
  }
}
