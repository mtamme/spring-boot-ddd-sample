package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.query.GetTicket;
import com.github.booking.application.ticket.query.ListTickets;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaTicketQueries {

  @NativeQuery(name = "Ticket.getTicket")
  Optional<GetTicket.Ticket> getTicket(@Param("ticket_id") String ticketId);

  @NativeQuery(name = "Ticket.listTickets")
  List<ListTickets.Ticket> listTickets(@Param("offset") long offset,
                                       @Param("limit") int limit);
}
