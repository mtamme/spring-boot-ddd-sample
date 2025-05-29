package com.github.booking.domain.hall;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record SeatNumber(String value) implements ValueObject {

  public SeatNumber {
    Contract.require(value != null);
  }
}
