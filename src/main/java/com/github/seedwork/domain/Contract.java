package com.github.seedwork.domain;

import java.util.function.Supplier;

public final class Contract {

  private Contract() {
  }

  public static void check(final boolean condition) {
    check(condition, IllegalStateException::new);
  }

  public static <E extends Throwable> void check(final boolean condition, final Supplier<E> exceptionSupplier) throws E {
    if (!condition) {
      throw exceptionSupplier.get();
    }
  }

  public static void require(final boolean condition) {
    require(condition, IllegalArgumentException::new);
  }

  public static <E extends Throwable> void require(final boolean condition, final Supplier<E> exceptionSupplier) throws E {
    if (!condition) {
      throw exceptionSupplier.get();
    }
  }
}
