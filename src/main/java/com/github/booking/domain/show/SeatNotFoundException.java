package com.github.booking.domain.show;

import com.github.seedwork.core.problem.NotFoundException;

public class SeatNotFoundException extends NotFoundException {

  public SeatNotFoundException() {
    super("Seat not found");
  }
}
