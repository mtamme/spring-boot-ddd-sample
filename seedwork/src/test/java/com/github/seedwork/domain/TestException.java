package com.github.seedwork.domain;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class TestException extends ProblemException {

  public static final Problem INVARIANT_PROBLEM = Problem.invariant("invariant", "Invariant violated");
  public static final Problem PRECONDITION_PROBLEM = Problem.precondition("precondition", "Precondition violated");

  private TestException(final Problem problem) {
    super(problem);
  }

  public static TestException invariant() {
    return new TestException(INVARIANT_PROBLEM);
  }

  public static TestException precondition() {
    return new TestException(PRECONDITION_PROBLEM);
  }
}
