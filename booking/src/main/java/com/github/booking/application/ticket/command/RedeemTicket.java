package com.github.booking.application.ticket.command;

import com.github.seedwork.application.CommandHandler;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RedeemTicket extends CommandHandler<RedeemTicket.Command> {

  record Command(String ticketId) {
  }
}
