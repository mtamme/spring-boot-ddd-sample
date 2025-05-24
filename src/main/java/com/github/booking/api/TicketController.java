package com.github.booking.api;

import com.github.booking.api.representation.GetTicketResponse;
import com.github.booking.api.representation.ListTicketsResponse;
import com.github.booking.api.representation.ShowAssignment;
import com.github.booking.api.representation.TicketBooking;
import com.github.booking.api.representation.TicketSummary;
import com.github.booking.application.ticket.TicketCommandHandler;
import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.command.RedeemTicketCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
public class TicketController implements TicketOperations {

  private final TicketCommandHandler ticketCommandHandler;
  private final TicketQueryHandler ticketQueryHandler;

  public TicketController(final TicketCommandHandler ticketCommandHandler, final TicketQueryHandler ticketQueryHandler) {
    this.ticketCommandHandler = Objects.requireNonNull(ticketCommandHandler);
    this.ticketQueryHandler = Objects.requireNonNull(ticketQueryHandler);
  }

  @Override
  public ResponseEntity<Void> redeemTicket(final String ticketId) {
    ticketCommandHandler.redeemTicket(new RedeemTicketCommand(ticketId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<GetTicketResponse> getTicket(final String ticketId) {
    final var ticket = ticketQueryHandler.getTicket(ticketId);
    final var response = new GetTicketResponse()
      .ticketId(ticket.ticketId())
      .status(ticket.status())
      .booking(Optional.ofNullable(ticket.booking())
        .map(b -> new TicketBooking()
          .bookingId(b.bookingId())
          .status(b.status()))
        .orElse(null))
      .showAssignment(Optional.ofNullable(ticket.showAssignment())
        .map(sa -> new ShowAssignment()
          .movieTitle(sa.movieTitle())
          .hallName(sa.hallName())
          .scheduledAt(sa.scheduledAt())
          .seatNumber(sa.seatNumber()))
        .orElse(null));

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ListTicketsResponse> listTickets(final Long offset, final Integer limit) {
    final var tickets = ticketQueryHandler.listTickets(offset, limit)
      .stream()
      .map(t -> new TicketSummary()
        .ticketId(t.ticketId())
        .status(t.status())
        .booking(Optional.ofNullable(t.booking())
          .map(tb -> new TicketBooking()
            .bookingId(tb.bookingId())
            .status(tb.status()))
          .orElse(null))
        .showAssignment(Optional.ofNullable(t.showAssignment())
          .map(tsa -> new ShowAssignment()
            .movieTitle(tsa.movieTitle())
            .hallName(tsa.hallName())
            .scheduledAt(tsa.scheduledAt())
            .seatNumber(tsa.seatNumber()))
          .orElse(null)))
      .toList();
    final var response = new ListTicketsResponse()
      .tickets(tickets);

    return ResponseEntity.ok(response);
  }
}
