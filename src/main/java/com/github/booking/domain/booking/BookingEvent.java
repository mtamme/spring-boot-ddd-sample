package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;
import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.Event;

public abstract class BookingEvent implements Event {

  private final ShowId showId;
  private final BookingId bookingId;

  protected BookingEvent(final ShowId showId, final BookingId bookingId) {
    this.showId = Contract.requireNonNull(showId);
    this.bookingId = Contract.requireNonNull(bookingId);
  }

  public ShowId showId() {
    return showId;
  }

  public BookingId bookingId() {
    return bookingId;
  }
}
