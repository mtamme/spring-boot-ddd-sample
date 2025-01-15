package com.github.booking.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class SeatNotReleasableException extends ProblemException {

  private static final Problem PROBLEM = new Problem("seat-not-releasable", "Seat not releasable");

  public SeatNotReleasableException() {
    super(PROBLEM);
  }
}
