package com.github.booking.domain.ticket;

import java.util.Optional;

public interface TicketRepository {

  TicketId nextTicketId();

  Optional<Ticket> findByTicketId(TicketId ticketId);

  void save(Ticket ticket);
}
