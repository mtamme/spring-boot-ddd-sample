package com.github.seedwork.core.util;

import java.util.function.Consumer;

public final class Consumers {

  private Consumers() {
  }

  public static <T> Consumer<T> noop() {
    return o -> {
      // Do nothing
    };
  }
}
