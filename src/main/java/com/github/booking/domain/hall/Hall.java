package com.github.booking.domain.hall;

import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.ValueObject;

public record Hall(HallId hallId, String name, SeatLayout seatLayout) implements ValueObject {

  public Hall {
    Contract.requireNonNull(hallId);
    Contract.requireNonNull(name);
    Contract.requireNonNull(seatLayout);
  }
}
