package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.seedwork.domain.Contract;

public class SeatBooked extends SeatEvent {

  private final BookingId bookingId;

  public SeatBooked(final ShowId showId, final SeatNumber seatNumber, final BookingId bookingId) {
    super(showId, seatNumber);
    Contract.require(bookingId != null);

    this.bookingId = bookingId;
  }

  public BookingId bookingId() {
    return bookingId;
  }
}
