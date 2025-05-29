package com.github.seedwork.infrastructure.outbox;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.Objects;

public class OutboxHealthIndicator implements HealthIndicator {

  private static final Status DEGRADED_STATUS = new Status("DEGRADED");

  private final OutboxProperties properties;
  private final MessageStore messageStore;

  public OutboxHealthIndicator(final OutboxProperties properties, final MessageStore messageStore) {
    this.properties = Objects.requireNonNull(properties);
    this.messageStore = Objects.requireNonNull(messageStore);
  }

  @Override
  public Health health() {
    final var poisonMessageCount = messageStore.count(properties.maxRequeueCount() + 1, Integer.MAX_VALUE);

    if (poisonMessageCount > 0) {
      return Health.status(DEGRADED_STATUS)
        .withDetail("reason", "Outbox contains %d poison %s which exceeded the maximum requeue count (%d)".formatted(
          poisonMessageCount,
          (poisonMessageCount == 1) ? "message" : "messages",
          properties.maxRequeueCount()))
        .build();
    }

    return Health.up()
      .withDetail("reason", "Outbox contains no poison messages which exceed the maximum requeue count (%d)".formatted(
        properties.maxRequeueCount()))
      .build();
  }
}
