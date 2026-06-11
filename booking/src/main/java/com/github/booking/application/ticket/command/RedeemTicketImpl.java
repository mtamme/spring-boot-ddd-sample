package com.github.booking.application.ticket.command;

import com.github.booking.domain.ticket.TicketException;
import com.github.booking.domain.ticket.TicketId;
import com.github.booking.domain.ticket.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class RedeemTicketImpl implements RedeemTicket {

  private final TicketRepository ticketRepository;

  public RedeemTicketImpl(final TicketRepository ticketRepository) {
    this.ticketRepository = Objects.requireNonNull(ticketRepository);
  }

  @Override
  public void handle(final Command command) {
    final var ticket = ticketRepository.findByTicketId(new TicketId(command.ticketId()))
      .orElseThrow(TicketException::notFound);

    ticket.redeem();
    ticketRepository.save(ticket);
  }
}
