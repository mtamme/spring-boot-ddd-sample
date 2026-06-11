package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.domain.ticket.Ticket;
import com.github.booking.domain.ticket.TicketId;
import com.github.booking.domain.ticket.TicketRepository;
import com.github.seedwork.core.util.Randoms;
import com.github.seedwork.infrastructure.persistence.repository.JpaAggregateRootSupport;
import org.springframework.data.repository.Repository;

public interface JpaTicketRepository extends JpaAggregateRootSupport, JpaTicketQueries, Repository<Ticket, Long>, TicketRepository {

  @Override
  default TicketId nextTicketId() {
    return new TicketId("T0%016X".formatted(Randoms.nextLong()));
  }

  @Override
  default void save(final Ticket ticket) {
    saveAndPublishEvents(ticket.ticketId().value(), ticket);
  }
}
