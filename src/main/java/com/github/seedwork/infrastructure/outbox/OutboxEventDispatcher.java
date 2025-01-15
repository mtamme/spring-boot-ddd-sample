package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.Event;
import com.github.seedwork.infrastructure.event.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class OutboxEventDispatcher implements EventDispatcher {

  private final Logger LOGGER = LoggerFactory.getLogger(OutboxEventDispatcher.class);

  private final MessageStore messageStore;

  public OutboxEventDispatcher(final MessageStore messageStore) {
    this.messageStore = Objects.requireNonNull(messageStore);
  }

  private void enqueueMessage(final String correlationId, final Serializable payload) {
    try {
      final var message = messageStore.enqueue(correlationId, payload);

      LOGGER.debug("Enqueued message (messageId={}, type={}, requeueCount={})", message.messageId(), message.type(), message.requeueCount());
    } catch (final Exception e) {
      LOGGER.error("Failed to enqueue message", e);

      throw e;
    }
  }

  @Override
  public void dispatchEvent(final Event event) {
    final var correlationId = String.valueOf(Thread.currentThread().threadId());

    enqueueMessage(correlationId, event);
  }
}
