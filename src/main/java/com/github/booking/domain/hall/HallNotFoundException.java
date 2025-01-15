package com.github.booking.domain.hall;

import com.github.seedwork.core.problem.NotFoundException;

public class HallNotFoundException extends NotFoundException {

  public HallNotFoundException() {
    super("Hall not found");
  }
}
