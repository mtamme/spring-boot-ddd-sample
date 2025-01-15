package com.github.seedwork.core.util;

import java.util.function.Consumer;

public final class Consumers {

  private Consumers() {
  }

  public static <T> Consumer<T> empty() {
    return o -> {
      // Do nothing
    };
  }
}
