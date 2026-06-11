package com.github.booking.infrastructure.web.ticket;

import com.github.booking.application.ticket.command.RedeemTicket;
import com.github.booking.application.ticket.query.GetTicket;
import com.github.booking.application.ticket.query.ListTickets;
import com.github.booking.infrastructure.web.TicketOperations;
import com.github.booking.infrastructure.web.representation.GetTicketResponse;
import com.github.booking.infrastructure.web.representation.ListTicketsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController implements TicketOperations {

  private final RedeemTicket redeemTicket;
  private final GetTicket getTicket;
  private final ListTickets listTickets;
  private final TicketMapper ticketMapper;

  public TicketController(final RedeemTicket redeemTicket,
                          final GetTicket getTicket,
                          final ListTickets listTickets,
                          final TicketMapper ticketMapper) {
    this.redeemTicket = redeemTicket;
    this.getTicket = getTicket;
    this.listTickets = listTickets;
    this.ticketMapper = ticketMapper;
  }

  @Override
  public ResponseEntity<Void> redeemTicket(final String ticketId) {
    redeemTicket.handle(new RedeemTicket.Command(ticketId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<GetTicketResponse> getTicket(final String ticketId) {
    final var ticket = getTicket.handle(new GetTicket.Query(ticketId));
    final var body = ticketMapper.toGetTicketResponse(ticket);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListTicketsResponse> listTickets(final Long offset, final Integer limit) {
    final var tickets = listTickets.handle(new ListTickets.Query(offset, limit));
    final var body = ticketMapper.toListTicketsResponse(tickets);

    return ResponseEntity.ok(body);
  }
}
