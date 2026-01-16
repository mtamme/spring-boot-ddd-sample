package com.github.booking.infrastructure.web;

import com.github.booking.application.ticket.TicketCommandHandler;
import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.command.RedeemTicketCommand;
import com.github.booking.application.ticket.query.GetTicketQuery;
import com.github.booking.application.ticket.query.ListTicketsQuery;
import com.github.booking.infrastructure.web.representation.GetTicketResponse;
import com.github.booking.infrastructure.web.representation.ListTicketsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class TicketController implements TicketOperations {

  private final TicketCommandHandler ticketCommandHandler;
  private final TicketQueryHandler ticketQueryHandler;
  private final TicketMapper ticketMapper;

  public TicketController(final TicketCommandHandler ticketCommandHandler,
                          final TicketQueryHandler ticketQueryHandler,
                          final TicketMapper ticketMapper) {
    this.ticketCommandHandler = Objects.requireNonNull(ticketCommandHandler);
    this.ticketQueryHandler = Objects.requireNonNull(ticketQueryHandler);
    this.ticketMapper = Objects.requireNonNull(ticketMapper);
  }

  @Override
  public ResponseEntity<Void> redeemTicket(final String ticketId) {
    ticketCommandHandler.redeemTicket(new RedeemTicketCommand(ticketId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<GetTicketResponse> getTicket(final String ticketId) {
    final var ticket = ticketQueryHandler.getTicket(new GetTicketQuery(ticketId));
    final var body = ticketMapper.toGetTicketResponse(ticket);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListTicketsResponse> listTickets(final Long offset, final Integer limit) {
    final var tickets = ticketQueryHandler.listTickets(new ListTicketsQuery(offset, limit));
    final var body = ticketMapper.toListTicketsResponse(tickets);

    return ResponseEntity.ok(body);
  }
}
