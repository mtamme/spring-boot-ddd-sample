package com.github.seedwork.core.problem;

import java.net.URI;
import java.util.Objects;

public record Problem(URI type, String message) {

  public Problem(final String type) {
    this(type, null);
  }

  public Problem(final String type, final String message) {
    this(URI.create("urn:" + type), message);
  }

  public Problem(final URI type, final String message) {
    this.type = Objects.requireNonNull(type);
    this.message = message;
  }

  public Problem withMessage(final String message) {
    return new Problem(type(), message);
  }

  public ProblemException toException() {
    return new ProblemException(this);
  }
}
