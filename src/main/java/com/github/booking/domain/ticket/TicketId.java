package com.github.booking.domain.ticket;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record TicketId(String value) implements ValueObject {

  public TicketId {
    Contract.require(value != null);
  }
}
