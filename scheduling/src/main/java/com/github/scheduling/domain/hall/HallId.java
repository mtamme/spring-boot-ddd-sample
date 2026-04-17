package com.github.scheduling.domain.hall;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record HallId(String value) implements ValueObject {

  public HallId {
    Contract.require(value != null);
  }
}
