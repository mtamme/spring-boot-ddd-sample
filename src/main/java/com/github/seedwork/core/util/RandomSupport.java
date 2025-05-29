package com.github.seedwork.core.util;

import java.util.random.RandomGenerator;

public final class RandomSupport {

  private static final RandomGenerator RANDOM_GENERATOR = RandomGenerator.of("L64X128MixRandom");

  private RandomSupport() {
  }

  public static long nextLong() {
    return RANDOM_GENERATOR.nextLong();
  }
}
