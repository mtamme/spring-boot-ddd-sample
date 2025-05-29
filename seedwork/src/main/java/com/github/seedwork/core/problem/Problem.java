package com.github.seedwork.core.problem;

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

public final class Problem implements Serializable, Supplier<ProblemException> {

  private static final String TYPE_FORMAT = "urn:problem:%s";

  private final ProblemKind kind;
  private final URI type;
  private final String message;

  private Problem(final ProblemKind kind, final String type, final String message) {
    this(kind, URI.create(TYPE_FORMAT.formatted(type)), message);
  }

  private Problem(final ProblemKind kind, final URI type, final String message) {
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

  public static Problem notPermitted(final String type, final String message) {
    return new Problem(ProblemKind.NOT_PERMITTED, type, message);
  }

  public static Problem of(final String type) {
    return new Problem(ProblemKind.NONE, type, null);
  }

  public static Problem of(final String type, final String message) {
    return new Problem(ProblemKind.NONE, type, message);
  }

  public ProblemKind kind() {
    return kind;
  }

  public URI type() {
    return type;
  }

  public String message() {
    return message;
  }

  public Problem withMessage(final String message) {
    return new Problem(kind(), type(), message);
  }

  @Override
  public ProblemException get() {
    return new ProblemException(this);
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
