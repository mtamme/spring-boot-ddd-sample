package com.github.booking.domain.ticket;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.Event;

public abstract class TicketEvent implements Event {

  private final TicketId ticketId;

  protected TicketEvent(final TicketId ticketId) {
    Contract.require(ticketId != null);

    this.ticketId = ticketId;
  }

  public TicketId ticketId() {
    return ticketId;
  }
}
