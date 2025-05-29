package com.github.seedwork.core.util;

import java.util.NoSuchElementException;
import java.util.Objects;

public sealed interface Result<S, F> permits Result.Success, Result.Failure {

  record Success<S, F>(S value) implements Result<S, F> {

    public Success {
      Objects.requireNonNull(value);
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public S get() {
      return value();
    }
  }

  record Failure<S, F>(F value) implements Result<S, F> {

    public Failure {
      Objects.requireNonNull(value);
    }

    @Override
    public boolean isFailure() {
      return true;
    }

    @Override
    public S get() {
      throw new NoSuchElementException("No success present");
    }
  }

  static <S, F> Success<S, F> success(final S value) {
    return new Success<>(value);
  }

  static <S, F> Failure<S, F> failure(final F value) {
    return new Failure<>(value);
  }

  default boolean isSuccess() {
    return false;
  }

  default boolean isFailure() {
    return false;
  }

  S get();
}
