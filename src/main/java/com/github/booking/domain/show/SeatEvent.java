package com.github.booking.domain.show;

import com.github.booking.domain.hall.SeatNumber;
import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.Event;

public abstract class SeatEvent implements Event {

  private final ShowId showId;
  private final SeatNumber seatNumber;

  protected SeatEvent(final ShowId showId, final SeatNumber seatNumber) {
    Contract.require(showId != null);
    Contract.require(seatNumber != null);

    this.showId = showId;
    this.seatNumber = seatNumber;
  }

  public ShowId showId() {
    return showId;
  }

  public SeatNumber seatNumber() {
    return seatNumber;
  }
}
