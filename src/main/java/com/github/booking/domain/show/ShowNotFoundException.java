package com.github.booking.domain.show;

import com.github.seedwork.core.problem.NotFoundException;

public class ShowNotFoundException extends NotFoundException {

  public ShowNotFoundException() {
    super("Show not found");
  }
}
