package com.github.booking.domain.movie;

import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.ValueObject;

public record MovieId(String value) implements ValueObject {

  public MovieId {
    Contract.requireNonNull(value);
  }
}
