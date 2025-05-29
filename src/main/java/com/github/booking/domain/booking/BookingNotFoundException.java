package com.github.booking.domain.booking;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class BookingNotFoundException extends ProblemException {

  private static final Problem PROBLEM = Problem.notFound("booking-not-found", "Booking not found");

  public BookingNotFoundException() {
    super(PROBLEM);
  }
}
