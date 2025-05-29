package com.github.seedwork.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OutboxProcessor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxProcessor.class);

  private final OutboxProperties properties;
  private final MessageConsumer messageConsumer;
  private final ApplicationEventPublisher eventPublisher;

  public OutboxProcessor(final OutboxProperties properties,
                         final MessageConsumer messageConsumer,
                         final ApplicationEventPublisher eventPublisher) {
    this.properties = Objects.requireNonNull(properties);
    this.messageConsumer = Objects.requireNonNull(messageConsumer);
    this.eventPublisher = Objects.requireNonNull(eventPublisher);
  }

  public void run(final UUID lockId) {
    final var sequenceNumbers = messageConsumer.lockAllNextActive(lockId, properties.lockLimit())
      .parallelStream()
      .filter(this::processMessage)
      .map(Message::sequenceNumber)
      .toList();

    dequeueMessages(sequenceNumbers, lockId);
  }

  private boolean processMessage(final Message message) {
    LOGGER.debug("Processing message (sequenceNumber={}, groupId={}, lockId={}, attemptCount={}, subject={})",
      message.sequenceNumber(),
      message.groupId(),
      message.lockId(),
      message.attemptCount(),
      message.subject());
    try {
      eventPublisher.publishEvent(message.body());
    } catch (final Throwable t) {
      LOGGER.error("Failed to process message (sequenceNumber={}, groupId={}, lockId={}, attemptCount={}, subject={})",
        message.sequenceNumber(),
        message.groupId(),
        message.lockId(),
        message.attemptCount(),
        message.subject(),
        t);

      return false;
    }

    return true;
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
