package com.github.booking.domain.show;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record ShowId(String value) implements ValueObject {

  public ShowId {
    Contract.require(value != null);
  }
}
