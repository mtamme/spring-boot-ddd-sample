package com.github.seedwork.infrastructure.outbox;

import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class TriggerContextFixture {

  private TriggerContextFixture() {
  }

  public static TriggerContext newTriggerContext(final Instant now) {
    final var clock = Clock.fixed(now, ZoneOffset.UTC);

    return new SimpleTriggerContext(clock);
  }

  public static TriggerContext newTriggerContextWithLastScheduledCompletion(final Instant lastScheduledExecution) {
    final var clock = Clock.fixed(lastScheduledExecution.plusMillis(15L), ZoneOffset.UTC);
    final var triggerContext = new SimpleTriggerContext(clock);

    triggerContext.update(
      lastScheduledExecution,
      lastScheduledExecution.plusMillis(5L),
      lastScheduledExecution.plusMillis(10L));

    return triggerContext;
  }
}
