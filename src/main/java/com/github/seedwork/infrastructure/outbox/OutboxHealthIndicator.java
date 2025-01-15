package com.github.seedwork.infrastructure.outbox;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Objects;

public class OutboxHealthIndicator implements HealthIndicator {

  private final OutboxProperties properties;
  private final MessageStore messageStore;

  public OutboxHealthIndicator(final OutboxProperties properties, final MessageStore messageStore) {
    this.properties = Objects.requireNonNull(properties);
    this.messageStore = Objects.requireNonNull(messageStore);
  }

  @Override
  public Health health() {
    final var poisonCount = messageStore.count(properties.maxRequeueCount() + 1, Integer.MAX_VALUE);

    if (poisonCount > 0) {
      return Health.down()
        .withDetail("message", "Outbox contains %d poison %s which exceeded the maximum requeue count (%d)".formatted(
          poisonCount,
          (poisonCount == 1) ? "message" : "messages",
          properties.maxRequeueCount()))
        .build();
    }

    return Health.up()
      .withDetail("message", "Outbox contains no poison messages which exceed the maximum requeue count (%d)".formatted(
        properties.maxRequeueCount()))
      .build();
  }
}
