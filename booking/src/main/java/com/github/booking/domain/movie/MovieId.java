package com.github.booking.domain.movie;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record MovieId(String value) implements ValueObject {

  public MovieId {
    Contract.require(value != null);
  }
}
