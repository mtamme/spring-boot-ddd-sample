package com.github.scheduling.domain.hall;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record Hall(HallId hallId, String name, int seatCount) implements ValueObject {

  public Hall {
    Contract.require(hallId != null);
    Contract.require((name != null) && !name.isBlank());
    Contract.require(seatCount > 0);
  }
}
