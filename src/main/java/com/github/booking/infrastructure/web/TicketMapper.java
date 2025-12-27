package com.github.booking.infrastructure.web;

import com.github.booking.application.ticket.query.TicketDetailView;
import com.github.booking.application.ticket.query.TicketSummaryView;
import com.github.booking.infrastructure.web.representation.GetTicketResponse;
import com.github.booking.infrastructure.web.representation.ListTicketsResponse;
import com.github.booking.infrastructure.web.representation.SeatAssignment;
import com.github.booking.infrastructure.web.representation.TicketBooking;
import com.github.booking.infrastructure.web.representation.TicketSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TicketMapper {

  private List<TicketSummary> toTicketSummaries(final List<TicketSummaryView> tickets) {
    return tickets.stream()
      .map(t -> new TicketSummary()
        .ticketId(t.ticketId())
        .status(t.status())
        .booking(Optional.ofNullable(t.booking())
          .map(tb -> new TicketBooking()
            .bookingId(tb.bookingId())
            .status(tb.status()))
          .orElse(null))
        .seatAssignment(Optional.ofNullable(t.seatAssignment())
          .map(tsa -> new SeatAssignment()
            .movieTitle(tsa.movieTitle())
            .hallName(tsa.hallName())
            .scheduledAt(tsa.scheduledAt())
            .seatNumber(tsa.seatNumber()))
          .orElse(null)))
      .toList();
  }

  public GetTicketResponse toGetTicketResponse(final TicketDetailView ticket) {
    return new GetTicketResponse()
      .ticketId(ticket.ticketId())
      .status(ticket.status())
      .booking(Optional.ofNullable(ticket.booking())
        .map(tb -> new TicketBooking()
          .bookingId(tb.bookingId())
          .status(tb.status()))
        .orElse(null))
      .seatAssignment(Optional.ofNullable(ticket.seatAssignment())
        .map(tsa -> new SeatAssignment()
          .movieTitle(tsa.movieTitle())
          .hallName(tsa.hallName())
          .scheduledAt(tsa.scheduledAt())
          .seatNumber(tsa.seatNumber()))
        .orElse(null));
  }

  public ListTicketsResponse toListTicketsResponse(final List<TicketSummaryView> tickets) {
    return new ListTicketsResponse()
      .tickets(toTicketSummaries(tickets));
  }
}
