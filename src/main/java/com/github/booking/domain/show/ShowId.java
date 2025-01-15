package com.github.booking.domain.show;

import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.ValueObject;

public record ShowId(String value) implements ValueObject {

  public ShowId {
    Contract.requireNonNull(value);
  }
}
