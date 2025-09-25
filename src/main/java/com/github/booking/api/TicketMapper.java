package com.github.booking.api;

import com.github.booking.api.representation.GetTicketResponse;
import com.github.booking.api.representation.ListTicketsResponse;
import com.github.booking.api.representation.TicketSummary;
import com.github.booking.application.ticket.view.TicketDetailView;
import com.github.booking.application.ticket.view.TicketSummaryView;

import java.util.List;

public interface TicketMapper {

  GetTicketResponse toGetTicketResponse(TicketDetailView ticket);

  default ListTicketsResponse toListTicketsResponse(final List<TicketSummaryView> tickets) {
    return new ListTicketsResponse()
      .tickets(toTicketSummaries(tickets));
  }

  List<TicketSummary> toTicketSummaries(List<TicketSummaryView> tickets);
}
