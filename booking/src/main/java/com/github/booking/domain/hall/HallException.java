package com.github.booking.domain.hall;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class HallException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("hall-not-found", "Hall not found");

  private HallException(final Problem problem) {
    super(problem);
  }

  public static HallException notFound() {
    return new HallException(NOT_FOUND_PROBLEM);
  }
}
