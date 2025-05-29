package com.github.booking.domain.show;

import com.github.booking.domain.hall.SeatNumber;

public class SeatReleased extends SeatEvent {

  public SeatReleased(final ShowId showId, final SeatNumber seatNumber) {
    super(showId, seatNumber);
  }
}
