package com.github.seedwork.infrastructure.outbox;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OutboxPollerTriggerTest {

  @Test
  void nextExecutionWithNoLastScheduledExecutionShouldReturnNow() {
    // Arrange
    final var outboxPollerTrigger = new OutboxPollerTrigger(Duration.ofSeconds(1L));
    final var triggerContext = TriggerContextFixture.newTriggerContext(Instant.EPOCH);

    // Act
    final var nextExecution = outboxPollerTrigger.nextExecution(triggerContext);

    // Assert
    assertEquals(Instant.EPOCH, nextExecution);
  }

  @Test
  void nextExecutionWithLastScheduledExecutionShouldReturnLastScheduledExecutionPlusPollInterval() {
    // Arrange
    final var outboxPollerTrigger = new OutboxPollerTrigger(Duration.ofSeconds(1L));
    final var triggerContext = TriggerContextFixture.newTriggerContextWithLastScheduledCompletion(Instant.EPOCH);

    // Act
    final var nextExecution = outboxPollerTrigger.nextExecution(triggerContext);

    // Assert
    assertEquals(Instant.EPOCH.plusSeconds(1L), nextExecution);
  }

  @Test
  void nextExecutionWithSkipNextPollIntervalAndLastScheduledExecutionShouldReturnLastScheduledExecution() {
    // Arrange
    final var outboxPollerTrigger = new OutboxPollerTrigger(Duration.ofSeconds(1L));

    outboxPollerTrigger.skipNextPollInterval();
    final var triggerContext = TriggerContextFixture.newTriggerContextWithLastScheduledCompletion(Instant.EPOCH);

    // Act
    final var nextExecution = outboxPollerTrigger.nextExecution(triggerContext);

    // Assert
    assertEquals(Instant.EPOCH, nextExecution);
  }
}
