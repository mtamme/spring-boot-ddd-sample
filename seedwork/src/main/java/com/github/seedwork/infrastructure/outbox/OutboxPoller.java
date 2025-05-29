package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.core.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.Trigger;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Gatherers;

public class OutboxPoller implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxPoller.class);

  private final OutboxProperties properties;
  private final MessageConsumer messageConsumer;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final OutboxPollerTrigger trigger;

  public OutboxPoller(final OutboxProperties properties,
                      final MessageConsumer messageConsumer,
                      final ApplicationEventPublisher applicationEventPublisher) {
    this.properties = Objects.requireNonNull(properties);
    this.messageConsumer = Objects.requireNonNull(messageConsumer);
    this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);

    this.trigger = new OutboxPollerTrigger(properties.pollInterval());
  }

  public Trigger trigger() {
    return trigger;
  }

  void run(final UUID lockId) {
    final var dispatchedSequenceNumbers = messageConsumer.lockAllNextActive(lockId, properties.lockLimit())
      .stream()
      .gather(Gatherers.mapConcurrent(properties.maxConcurrency(), this::dispatchMessage))
      .filter(Result::isSuccess)
      .map(Result::get)
      .toList();

    dequeueMessages(dispatchedSequenceNumbers, lockId);
  }

  private Result<Long, Throwable> dispatchMessage(final Message message) {
    LOGGER.debug("Dispatching message (sequenceNumber={}, groupId={}, lockId={}, attemptCount={}, subject={})",
      message.sequenceNumber(),
      message.groupId(),
      message.lockId(),
      message.attemptCount(),
      message.subject());
    try {
      applicationEventPublisher.publishEvent(message.body());
    } catch (final Throwable t) {
      LOGGER.error("Failed to dispatch message (sequenceNumber={}, groupId={}, lockId={}, attemptCount={}, subject={})",
        message.sequenceNumber(),
        message.groupId(),
        message.lockId(),
        message.attemptCount(),
        message.subject(),
        t);

      return Result.failure(t);
    }

    return Result.success(message.sequenceNumber());
  }

  private void dequeueMessages(final List<Long> sequenceNumbers, final UUID lockId) {
    if (sequenceNumbers.isEmpty()) {
      return;
    }
    LOGGER.debug("Dequeuing messages (sequenceNumbers={})", sequenceNumbers);

    try {
      messageConsumer.dequeueAllLocked(sequenceNumbers, lockId);
      trigger.skipNextPollInterval();
    } catch (final Exception e) {
      LOGGER.error("Failed to dequeue messages (sequenceNumbers={})", sequenceNumbers, e);
    }
  }

  @Override
  public void run() {
    run(UUID.randomUUID());
  }
}
