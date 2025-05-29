package com.github.booking.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class ShowException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("show-not-found", "Show not found");

  private ShowException(final Problem problem) {
    super(problem);
  }

  public static ShowException notFound() {
    return new ShowException(NOT_FOUND_PROBLEM);
  }
}
