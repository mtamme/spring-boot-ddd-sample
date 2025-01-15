package com.github.booking.domain.booking;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class BookingNotCancelableException extends ProblemException {

  private static final Problem PROBLEM = new Problem("booking-not-cancelable", "Booking not cancelable");

  public BookingNotCancelableException() {
    super(PROBLEM);
  }
}
