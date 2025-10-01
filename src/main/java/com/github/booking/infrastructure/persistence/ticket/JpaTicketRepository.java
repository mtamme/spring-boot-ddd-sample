package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.view.TicketDetailView;
import com.github.booking.application.ticket.view.TicketSummaryView;
import com.github.booking.domain.ticket.Ticket;
import com.github.booking.domain.ticket.TicketId;
import com.github.booking.domain.ticket.TicketRepository;
import com.github.seedwork.core.util.Base64Support;
import com.github.seedwork.core.util.RandomSupport;
import com.github.seedwork.infrastructure.persistence.JpaAggregateSaver;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaTicketRepository extends JpaAggregateSaver<Ticket>, Repository<Ticket, Long>, TicketRepository {

  @NativeQuery(name = "TicketDetailView.getTicket")
  Optional<TicketDetailView> getTicket(@Param("ticket_id") String ticketId);

  @NativeQuery(name = "TicketSummaryView.listTickets")
  List<TicketSummaryView> listTickets(@Param("offset") long offset,
                                      @Param("limit") int limit);

  @Override
  default TicketId nextTicketId() {
    return new TicketId(Base64Support.encodeLong(RandomSupport.nextLong()));
  }

  @Override
  default void save(final Ticket ticket) {
    saveAndPublishEvents(ticket.ticketId().value(), ticket);
  }
}
