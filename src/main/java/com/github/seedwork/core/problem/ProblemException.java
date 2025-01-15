package com.github.seedwork.core.problem;

public class ProblemException extends RuntimeException {

  private final Problem problem;

  public ProblemException(final Problem problem) {
    super(problem.message());

    this.problem = problem;
  }

  public ProblemException(final Problem problem, final Throwable cause) {
    super(problem.message(), cause);

    this.problem = problem;
  }

  public Problem getProblem() {
    return problem;
  }
}
