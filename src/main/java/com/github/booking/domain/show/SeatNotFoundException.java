package com.github.booking.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class SeatNotFoundException extends ProblemException {

  private static final Problem PROBLEM = Problem.notFound("seat-not-found", "Seat not found");

  public SeatNotFoundException() {
    super(PROBLEM);
  }
}
