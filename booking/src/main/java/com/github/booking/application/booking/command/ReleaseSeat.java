package com.github.booking.application.booking.command;

import com.github.seedwork.application.CommandHandler;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ReleaseSeat extends CommandHandler<ReleaseSeat.Command> {

  record Command(String bookingId, String seatNumber) {
  }
}
