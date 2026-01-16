package com.github.booking.application.ticket;

import com.github.booking.application.ticket.command.RedeemTicketCommand;
import com.github.booking.domain.ticket.TicketException;
import com.github.booking.domain.ticket.TicketId;
import com.github.booking.domain.ticket.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TicketCommandHandlerImpl implements TicketCommandHandler {

  private final TicketRepository ticketRepository;

  public TicketCommandHandlerImpl(final TicketRepository ticketRepository) {
    this.ticketRepository = Objects.requireNonNull(ticketRepository);
  }

  @Override
  public void redeemTicket(final RedeemTicketCommand command) {
    final var ticket = ticketRepository.findByTicketId(new TicketId(command.ticketId()))
      .orElseThrow(TicketException::notFound);

    ticket.redeem();
    ticketRepository.save(ticket);
  }
}
