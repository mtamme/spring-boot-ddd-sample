package com.github.seedwork.core.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public final class Longs {

  private static final Random RANDOM = new SecureRandom();

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder()
    .withoutPadding();

  private Longs() {
  }

  public static long nextLong() {
    return RANDOM.nextLong();
  }

  public static long fromBase64String(final String string) {
    final var buffer = BASE64_DECODER.decode(string);

    return ByteBuffer.wrap(buffer)
      .getLong();
  }

  public static String toBase64String(final long value) {
    final var buffer = ByteBuffer.allocate(Long.BYTES)
      .putLong(value)
      .flip()
      .array();

    return BASE64_ENCODER.encodeToString(buffer);
  }
}
