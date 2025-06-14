package com.github.booking.application.ticket;

import com.github.booking.application.ticket.view.TicketDetailView;
import com.github.booking.application.ticket.view.TicketSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface TicketQueryHandler {

  TicketDetailView getTicket(String ticketId);

  List<TicketSummaryView> listTickets(long offset, int limit);
}
