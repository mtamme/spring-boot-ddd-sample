package com.github.seedwork.infrastructure.web;

import com.github.seedwork.core.problem.ProblemType;

public final class ProblemTypes {

  public static final ProblemType INVALID = ProblemType.of("invalid");
  public static final ProblemType MALFORMED = ProblemType.of("malformed");
  public static final ProblemType NOT_FOUND = ProblemType.of("not-found");
  public static final ProblemType METHOD_NOT_ALLOWED = ProblemType.of("method-not-allowed");
  public static final ProblemType NOT_ACCEPTABLE = ProblemType.of("not-acceptable");
  public static final ProblemType LOCKING = ProblemType.of("locking");
  public static final ProblemType DUPLICATE = ProblemType.of("duplicate");
  public static final ProblemType INTERNAL = ProblemType.of("internal");

  private ProblemTypes() {
  }
}
