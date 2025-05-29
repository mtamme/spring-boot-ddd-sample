package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.Event;
import com.github.seedwork.infrastructure.event.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class OutboxEventDispatcher implements EventDispatcher {

  private final Logger LOGGER = LoggerFactory.getLogger(OutboxEventDispatcher.class);

  private final MessageProducer messageProducer;

  public OutboxEventDispatcher(final MessageProducer messageProducer) {
    this.messageProducer = Objects.requireNonNull(messageProducer);
  }

  private void enqueueMessage(final String groupId, final String subject, final Serializable body) {
    try {
      final var message = messageProducer.enqueue(groupId, subject, body);

      LOGGER.debug("Enqueued message (sequenceNumber={}, groupId={}, subject={})",
        message.sequenceNumber(),
        message.groupId(),
        message.subject());
    } catch (final Exception e) {
      LOGGER.error("Failed to enqueue message (groupId={}, subject={})", groupId, subject, e);

      throw e;
    }
  }

  @Override
  public void dispatchEvent(final String groupId, final Event event) {
    enqueueMessage(groupId, event.getClass().getSimpleName(), event);
  }
}
