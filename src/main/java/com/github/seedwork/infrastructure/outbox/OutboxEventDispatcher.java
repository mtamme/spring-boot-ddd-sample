package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.Event;
import com.github.seedwork.infrastructure.event.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public class OutboxEventDispatcher implements EventDispatcher {

  private final Logger LOGGER = LoggerFactory.getLogger(OutboxEventDispatcher.class);

  private final Clock clock;
  private final MessageStore messageStore;

  public OutboxEventDispatcher(final Clock clock, final MessageStore messageStore) {
    this.clock = Objects.requireNonNull(clock);
    this.messageStore = Objects.requireNonNull(messageStore);
  }

  private void enqueueMessage(final Serializable payload) {
    try {
      final var message = new Message(
        UUID.randomUUID(),
        String.valueOf(Thread.currentThread().threadId()),
        payload.getClass().getSimpleName(),
        clock.instant(),
        payload);

      LOGGER.info("Enqueueing message (messageId={}, type={}, requeueCount={})", message.messageId(), message.type(), message.requeueCount());
      messageStore.enqueue(message);
    } catch (final Exception e) {
      LOGGER.error("Failed to enqueue message", e);

      throw e;
    }
  }

  @Override
  public void dispatchEvent(final Event event) {
    enqueueMessage(event);
  }
}
