package com.github.booking.api;

import com.github.booking.api.representation.GetTicketResponse;
import com.github.booking.api.representation.SeatAssignment;
import com.github.booking.api.representation.TicketBooking;
import com.github.booking.api.representation.TicketSummary;
import com.github.booking.application.ticket.view.TicketDetailView;
import com.github.booking.application.ticket.view.TicketSummaryView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TicketMapperImpl implements TicketMapper {

  @Override
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

  @Override
  public List<TicketSummary> toTicketSummaries(final List<TicketSummaryView> tickets) {
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
}
