package com.github.seedwork.core.util;

import java.security.SecureRandom;
import java.util.Random;

public final class RandomSupport {

  private static final Random RANDOM = new SecureRandom();

  private RandomSupport() {
  }

  public static long nextLong() {
    return RANDOM.nextLong();
  }
}
