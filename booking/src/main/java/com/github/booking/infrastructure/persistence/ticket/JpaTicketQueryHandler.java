package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.query.GetTicketQuery;
import com.github.booking.application.ticket.query.ListTicketsQuery;
import com.github.booking.application.ticket.query.TicketDetailView;
import com.github.booking.application.ticket.query.TicketSummaryView;
import com.github.booking.domain.ticket.TicketException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JpaTicketQueryHandler implements TicketQueryHandler {

  private final JpaTicketRepository repository;

  public JpaTicketQueryHandler(final JpaTicketRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public TicketDetailView getTicket(final GetTicketQuery query) {
    return repository.getTicket(query.ticketId())
      .orElseThrow(TicketException::notFound);
  }

  @Override
  public List<TicketSummaryView> listTickets(final ListTicketsQuery query) {
    return repository.listTickets(query.offset(), query.limit());
  }
}
