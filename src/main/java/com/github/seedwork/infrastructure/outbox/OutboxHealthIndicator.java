package com.github.seedwork.infrastructure.outbox;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;

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
      .withDetail("failedMessages", messageCounts.failedCount())
      .withDetail("lockedMessages", messageCounts.lockedCount())
      .withDetail("totalMessages", messageCounts.totalCount())
      .build();
  }
}
