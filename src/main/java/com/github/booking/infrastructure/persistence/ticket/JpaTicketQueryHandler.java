package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.query.TicketDetailView;
import com.github.booking.application.ticket.query.TicketSummaryView;
import com.github.booking.domain.ticket.TicketNotFoundException;
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
  public TicketDetailView getTicket(final String ticketId) {
    return repository.getTicket(ticketId)
      .orElseThrow(TicketNotFoundException::new);
  }

  @Override
  public List<TicketSummaryView> listTickets(final long offset, final int limit) {
    return repository.listTickets(offset, limit);
  }
}
