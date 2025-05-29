package com.github.booking.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class ShowNotFoundException extends ProblemException {

  private static final Problem PROBLEM = Problem.notFound("show-not-found", "Show not found");

  public ShowNotFoundException() {
    super(PROBLEM);
  }
}
