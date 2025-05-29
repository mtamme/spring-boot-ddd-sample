package com.github.seedwork.core.problem;

import java.io.Serializable;
import java.util.Objects;

public final class Problem implements Serializable {

  private final ProblemKind kind;
  private final ProblemType type;
  private final String message;

  private Problem(final ProblemKind kind, final String type, final String message) {
    this(kind, ProblemType.of(type), message);
  }

  private Problem(final ProblemKind kind, final ProblemType type, final String message) {
    this.kind = Objects.requireNonNull(kind);
    this.type = Objects.requireNonNull(type);
    this.message = message;
  }

  public static Problem invariant(final String type, final String message) {
    return new Problem(ProblemKind.INVARIANT, type, message);
  }

  public static Problem precondition(final String type, final String message) {
    return new Problem(ProblemKind.PRECONDITION, type, message);
  }

  public static Problem notFound(final String type, final String message) {
    return new Problem(ProblemKind.NOT_FOUND, type, message);
  }

  public ProblemKind kind() {
    return kind;
  }

  public ProblemType type() {
    return type;
  }

  public String message() {
    return message;
  }

  public Problem withMessage(final String message) {
    return new Problem(kind(), type(), message);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof final Problem other)) {
      return false;
    }

    return Objects.equals(other.kind(), kind()) &&
      Objects.equals(other.type(), type());
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind(), type());
  }
}
