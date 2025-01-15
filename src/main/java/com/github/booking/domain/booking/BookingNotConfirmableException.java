package com.github.booking.domain.booking;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class BookingNotConfirmableException extends ProblemException {

  private static final Problem PROBLEM = new Problem("booking-not-confirmable", "Booking not confirmable");

  public BookingNotConfirmableException() {
    super(PROBLEM);
  }
}
