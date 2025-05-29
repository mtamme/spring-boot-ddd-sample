package com.github.booking.application.booking;

import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingResult;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BookingCommandHandler {

  InitiateBookingResult initiateBooking(InitiateBookingCommand command);

  void reserveSeat(ReserveSeatCommand command);

  void releaseSeat(ReleaseSeatCommand command);

  void confirmBooking(ConfirmBookingCommand command);

  void cancelBooking(CancelBookingCommand command);
}
