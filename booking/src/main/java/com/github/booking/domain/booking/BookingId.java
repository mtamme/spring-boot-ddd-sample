package com.github.booking.domain.booking;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record BookingId(String value) implements ValueObject {

  public BookingId {
    Contract.require(value != null);
  }
}
