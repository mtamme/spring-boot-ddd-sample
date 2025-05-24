package com.github.booking.domain.ticket;

public class TicketIssued extends TicketEvent {

  public TicketIssued(final TicketId ticketId) {
    super(ticketId);
  }
}
