package com.github.seedwork.infrastructure.outbox;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.Objects;

public class OutboxHealthIndicator implements HealthIndicator {

  private final MessageConsumer messageConsumer;

  public OutboxHealthIndicator(final MessageConsumer messageConsumer) {
    this.messageConsumer = Objects.requireNonNull(messageConsumer);
  }

  @Override
  public Health health() {
    final var messageCounts = messageConsumer.count();

    return Health.status(messageCounts.hasFailed() ? Status.UNKNOWN : Status.UP)
      .withDetail("activeMessages", messageCounts.activeCount())
      .withDetail("activeLockedMessages", messageCounts.activeLockedCount())
      .withDetail("failedMessages", messageCounts.failedCount())
      .withDetail("failedLockedMessages", messageCounts.failedLockedCount())
      .withDetail("totalMessages", messageCounts.totalCount())
      .build();
  }
}
