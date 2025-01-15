package com.github.seedwork.core.problem;

public class NotFoundException extends ProblemException {

  private static final Problem NOT_FOUND_PROBLEM = new Problem("not-found");

  public NotFoundException(final String message) {
    super(NOT_FOUND_PROBLEM.withMessage(message));
  }

  public NotFoundException(final String message, final Throwable cause) {
    super(NOT_FOUND_PROBLEM.withMessage(message), cause);
  }
}
