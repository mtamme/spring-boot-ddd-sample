package com.github.booking.application.booking.command;

import com.github.seedwork.application.CommandHandler;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CancelBooking extends CommandHandler<CancelBooking.Command> {

  record Command(String bookingId) {
  }
}
