package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.seedwork.domain.Contract;

public class SeatReserved extends SeatEvent {

  private final BookingId bookingId;

  public SeatReserved(final ShowId showId, final SeatNumber seatNumber, final BookingId bookingId) {
    super(showId, seatNumber);
    Contract.require(bookingId != null);

    this.bookingId = bookingId;
  }

  public BookingId bookingId() {
    return bookingId;
  }
}
