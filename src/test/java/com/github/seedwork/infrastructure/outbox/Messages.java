package com.github.seedwork.infrastructure.outbox;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class Messages {

  private Messages() {
  }

  public static Message newMessage(final String messageId,
                                   final String correlationId,
                                   final Instant enqueuedAt,
                                   final Duration delay,
                                   final int requeueCount,
                                   final Serializable payload) {
    return new Message(
      UUID.fromString(messageId),
      correlationId,
      enqueuedAt,
      enqueuedAt.plus(delay),
      requeueCount,
      payload);
  }
}
