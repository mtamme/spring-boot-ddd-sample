package com.github.seedwork.infrastructure.outbox;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class OutboxPollerTrigger implements Trigger {

  private final Duration pollInterval;
  private final AtomicBoolean skipNextPollInterval;

  public OutboxPollerTrigger(final Duration pollInterval) {
    this.pollInterval = Objects.requireNonNull(pollInterval);

    this.skipNextPollInterval = new AtomicBoolean(false);
  }

  public void skipNextPollInterval() {
    skipNextPollInterval.set(true);
  }

  @Override
  public @Nullable Instant nextExecution(@NonNull final TriggerContext triggerContext) {
    final var lastScheduledExecution = triggerContext.lastScheduledExecution();

    if (lastScheduledExecution == null) {
      final var clock = triggerContext.getClock();

      return clock.instant();
    }
    if (skipNextPollInterval.compareAndSet(true, false)) {
      return lastScheduledExecution;
    }

    return lastScheduledExecution.plus(pollInterval);
  }
}
