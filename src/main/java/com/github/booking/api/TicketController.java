package com.github.booking.api;

import com.github.booking.api.representation.GetTicketResponse;
import com.github.booking.api.representation.ListTicketsResponse;
import com.github.booking.application.ticket.TicketCommandHandler;
import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.command.RedeemTicketCommand;
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
    final var ticket = ticketQueryHandler.getTicket(ticketId);
    final var body = ticketMapper.toGetTicketResponse(ticket);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListTicketsResponse> listTickets(final Long offset, final Integer limit) {
    final var tickets = ticketQueryHandler.listTickets(offset, limit);
    final var body = ticketMapper.toListTicketsResponse(tickets);

    return ResponseEntity.ok(body);
  }
}
