package com.github.booking.domain.booking;

import com.github.seedwork.core.problem.NotFoundException;

public class BookingNotFoundException extends NotFoundException {

  public BookingNotFoundException() {
    super("Booking not found");
  }
}
