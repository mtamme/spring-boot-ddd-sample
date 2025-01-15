package com.github.seedwork.core.problem;

public class NotFoundException extends ProblemException {

  private static final Problem PROBLEM = new Problem("not-found");

  public NotFoundException(final String message) {
    super(PROBLEM.withMessage(message));
  }

  public NotFoundException(final String message, final Throwable cause) {
    super(PROBLEM.withMessage(message), cause);
  }
}
