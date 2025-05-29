package com.github.booking.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class SeatNotReservableException extends ProblemException {

  private static final Problem PROBLEM = Problem.conflict("seat-not-reservable", "Seat not reservable");

  public SeatNotReservableException() {
    super(PROBLEM);
  }
}
