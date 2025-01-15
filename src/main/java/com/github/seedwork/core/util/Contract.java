package com.github.seedwork.core.util;

import java.util.function.Supplier;

public final class Contract {

  private Contract() {
  }

  public static void require(final boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  public static <E extends Throwable> void require(final boolean expression, final Supplier<E> exceptionSupplier) throws E {
    if (!expression) {
      throw exceptionSupplier.get();
    }
  }

  public static <T> T requireNonNull(final T value) {
    if (value == null) {
      throw new IllegalArgumentException();
    }

    return value;
  }
}
