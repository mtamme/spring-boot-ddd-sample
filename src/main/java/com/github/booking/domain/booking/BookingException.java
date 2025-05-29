package com.github.booking.domain.booking;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class BookingException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("booking-not-found", "Booking not found");
  public static final Problem NOT_CANCELABLE_PROBLEM = Problem.conflict("booking-not-cancelable", "Booking not cancelable");
  public static final Problem NOT_CONFIRMABLE_PROBLEM = Problem.conflict("booking-not-confirmable", "Booking not confirmable");

  private BookingException(final Problem problem) {
    super(problem);
  }

  public static BookingException notFound() {
    return new BookingException(NOT_FOUND_PROBLEM);
  }

  public static BookingException notCancelable() {
    return new BookingException(NOT_CANCELABLE_PROBLEM);
  }

  public static BookingException notConfirmable() {
    return new BookingException(NOT_CONFIRMABLE_PROBLEM);
  }
}
