package com.github.booking.application.ticket;

import com.github.booking.application.ticket.query.GetTicketQuery;
import com.github.booking.application.ticket.query.ListTicketsQuery;
import com.github.booking.application.ticket.query.TicketDetailView;
import com.github.booking.application.ticket.query.TicketSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface TicketQueryHandler {

  TicketDetailView getTicket(GetTicketQuery query);

  List<TicketSummaryView> listTickets(ListTicketsQuery query);
}
