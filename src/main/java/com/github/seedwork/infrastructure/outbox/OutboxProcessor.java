package com.github.seedwork.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutboxProcessor implements Runnable {

  private final Logger LOGGER = LoggerFactory.getLogger(OutboxProcessor.class);

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

  private Stream<Message> processMessagesOrderedUntilFailure(final List<Message> messages) {
    return messages.stream()
      .takeWhile(this::tryProcessMessage);
  }

  private boolean tryProcessMessage(final Message message) {
    LOGGER.debug("Processing message (sequenceNumber={}, groupId={}, lockId={}, deliveryCount={}, subject={})",
      message.sequenceNumber(),
      message.groupId(),
      message.lockId(),
      message.deliveryCount(),
      message.subject());
    try {
      eventPublisher.publishEvent(message.body());
    } catch (final Throwable t) {
      LOGGER.error("Failed to process message (sequenceNumber={}, groupId={}, lockId={}, deliveryCount={}, subject={})",
        message.sequenceNumber(),
        message.groupId(),
        message.lockId(),
        message.deliveryCount(),
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
      messageConsumer.dequeueAll(sequenceNumbers, lockId);
    } catch (final Exception e) {
      LOGGER.error("Failed to dequeue messages (sequenceNumbers={})", sequenceNumbers, e);
    }
  }

  public void run(final UUID lockId) {
    final var messages = messageConsumer.lockAllDeliverable(lockId, properties.lockLimit());

    if (messages.isEmpty()) {
      return;
    }
    final var sequenceNumbers = messages.stream()
      .collect(Collectors.groupingBy(Message::groupId, Collectors.toUnmodifiableList()))
      .values()
      .parallelStream()
      .flatMap(this::processMessagesOrderedUntilFailure)
      .map(Message::sequenceNumber)
      .toList();

    dequeueMessages(sequenceNumbers, lockId);
  }

  @Override
  public void run() {
    run(UUID.randomUUID());
  }
}
