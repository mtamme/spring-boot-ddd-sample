package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.query.ListTickets;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
class JpaListTickets implements ListTickets {

  private final JpaTicketQueries queries;

  public JpaListTickets(final JpaTicketQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public List<Ticket> handle(final Query query) {
    return queries.listTickets(query.offset(), query.limit());
  }
}
