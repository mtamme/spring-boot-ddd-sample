package com.github.booking.application.ticket;

import com.github.booking.application.ticket.command.RedeemTicketCommand;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TicketCommandHandler {

  void redeemTicket(RedeemTicketCommand command);
}
