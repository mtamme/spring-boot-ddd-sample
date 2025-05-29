package com.github.seedwork.core.problem;

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

public final class ProblemType implements Serializable {

  private static final String URI_FORMAT = "urn:problem:%s";

  private final URI uri;

  private ProblemType(final URI uri) {
    this.uri = Objects.requireNonNull(uri);
  }

  public static ProblemType of(final String string) {
    final var uri = URI.create(URI_FORMAT.formatted(string));

    return new ProblemType(uri);
  }

  public URI uri() {
    return uri;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof ProblemType other)) {
      return false;
    }

    return Objects.equals(other.uri(), uri());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(uri());
  }

  @Override
  public String toString() {
    return uri().toString();
  }
}
