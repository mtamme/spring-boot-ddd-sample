package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.query.GetTicket;
import com.github.booking.domain.ticket.TicketException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class JpaGetTicket implements GetTicket {

  private final JpaTicketQueries queries;

  public JpaGetTicket(final JpaTicketQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public Ticket handle(final Query query) {
    return queries.getTicket(query.ticketId())
      .orElseThrow(TicketException::notFound);
  }
}
