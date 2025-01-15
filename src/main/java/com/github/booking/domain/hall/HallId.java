package com.github.booking.domain.hall;

import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.ValueObject;

public record HallId(String value) implements ValueObject {

  public HallId {
    Contract.requireNonNull(value);
  }
}
