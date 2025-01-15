package com.github.booking.domain.hall;

import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.ValueObject;

public record SeatNumber(String value) implements ValueObject {

  public SeatNumber {
    Contract.requireNonNull(value);
  }
}
