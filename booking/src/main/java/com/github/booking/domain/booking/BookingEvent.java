package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;
import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.Event;

public abstract class BookingEvent implements Event {

  private final ShowId showId;
  private final BookingId bookingId;

  protected BookingEvent(final ShowId showId, final BookingId bookingId) {
    Contract.require(showId != null);
    Contract.require(bookingId != null);

    this.showId = showId;
    this.bookingId = bookingId;
  }

  public ShowId showId() {
    return showId;
  }

  public BookingId bookingId() {
    return bookingId;
  }
}
