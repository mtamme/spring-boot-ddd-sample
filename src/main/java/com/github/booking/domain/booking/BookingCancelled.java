package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;

public class BookingCancelled extends BookingEvent {

  public BookingCancelled(final ShowId showId, final BookingId bookingId) {
    super(showId, bookingId);
  }
}
