package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.core.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Gatherers;

public class OutboxProcessor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxProcessor.class);

  private final OutboxProperties properties;
  private final MessageConsumer messageConsumer;
  private final ApplicationEventPublisher applicationEventPublisher;

  public OutboxProcessor(final OutboxProperties properties,
                         final MessageConsumer messageConsumer,
                         final ApplicationEventPublisher applicationEventPublisher) {
    this.properties = Objects.requireNonNull(properties);
    this.messageConsumer = Objects.requireNonNull(messageConsumer);
    this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
  }

  public void run(final UUID lockId) {
    final var sequenceNumbers = messageConsumer.lockAllNextActive(lockId, properties.lockLimit())
      .stream()
      .gather(Gatherers.mapConcurrent(properties.maxConcurrency(), this::processMessage))
      .filter(Result::isSuccess)
      .map(Result::get)
      .toList();

    dequeueMessages(sequenceNumbers, lockId);
  }

  private Result<Long, Throwable> processMessage(final Message message) {
    LOGGER.debug("Processing message (sequenceNumber={}, groupId={}, lockId={}, attemptCount={}, subject={})",
      message.sequenceNumber(),
      message.groupId(),
      message.lockId(),
      message.attemptCount(),
      message.subject());
    try {
      applicationEventPublisher.publishEvent(message.body());
    } catch (final Throwable t) {
      LOGGER.error("Failed to process message (sequenceNumber={}, groupId={}, lockId={}, attemptCount={}, subject={})",
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
    } catch (final Exception e) {
      LOGGER.error("Failed to dequeue messages (sequenceNumbers={})", sequenceNumbers, e);
    }
  }

  @Override
  public void run() {
    run(UUID.randomUUID());
  }
}
