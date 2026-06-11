package com.github.booking.domain.ticket;

public class TicketRedeemed extends TicketEvent {

  public TicketRedeemed(final TicketId ticketId) {
    super(ticketId);
  }
}
