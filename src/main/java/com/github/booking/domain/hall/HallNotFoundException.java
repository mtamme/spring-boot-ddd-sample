package com.github.booking.domain.hall;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class HallNotFoundException extends ProblemException {

  private static final Problem PROBLEM = Problem.notFound("hall-not-found", "Hall not found");

  public HallNotFoundException() {
    super(PROBLEM);
  }
}
