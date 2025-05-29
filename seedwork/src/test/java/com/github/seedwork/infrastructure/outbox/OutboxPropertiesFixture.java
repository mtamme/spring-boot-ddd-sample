package com.github.seedwork.infrastructure.outbox;

import java.time.Duration;

public final class OutboxPropertiesFixture {

  private OutboxPropertiesFixture() {
  }

  public static OutboxProperties newOutboxProperties() {
    return new OutboxProperties(
      true,
      Duration.ofSeconds(1L),
      Duration.ofSeconds(30L),
      100,
      10,
      10);
  }
}
