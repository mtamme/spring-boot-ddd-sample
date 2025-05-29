package com.github.booking.application.booking.command;

import com.github.seedwork.application.CommandHandlerWithResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface InitiateBooking extends CommandHandlerWithResult<InitiateBooking.Command, InitiateBooking.Booking> {

  record Command(String showId) {
  }

  record Booking(String bookingId) {
  }
}
