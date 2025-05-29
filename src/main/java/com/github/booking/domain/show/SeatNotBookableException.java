package com.github.booking.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class SeatNotBookableException extends ProblemException {

  private static final Problem PROBLEM = Problem.conflict("seat-not-bookable", "Seat not bookable");

  public SeatNotBookableException() {
    super(PROBLEM);
  }
}
