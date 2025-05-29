package com.github.seedwork.core.util;

import java.nio.ByteBuffer;
import java.util.Base64;

public final class Base64Support {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder()
    .withoutPadding();

  private Base64Support() {
  }

  public static byte[] decodeBytes(final String string) {
    return BASE64_DECODER.decode(string);
  }

  public static long decodeLong(final String string) {
    final var buffer = decodeBytes(string);

    return ByteBuffer.wrap(buffer)
      .getLong();
  }

  public static String encodeBytes(final byte[] buffer) {
    return BASE64_ENCODER.encodeToString(buffer);
  }

  public static String encodeLong(final long value) {
    final var buffer = ByteBuffer.allocate(Long.BYTES)
      .putLong(value)
      .flip()
      .array();

    return encodeBytes(buffer);
  }
}
