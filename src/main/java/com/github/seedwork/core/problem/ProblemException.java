package com.github.seedwork.core.problem;

import java.util.Objects;

public class ProblemException extends RuntimeException {

  private final Problem problem;

  public ProblemException(final Problem problem) {
    this(problem, null);
  }

  public ProblemException(final Problem problem, final Throwable cause) {
    super((problem != null) ? problem.message() : null, cause);

    this.problem = Objects.requireNonNull(problem);
  }

  public Problem getProblem() {
    return problem;
  }
}
