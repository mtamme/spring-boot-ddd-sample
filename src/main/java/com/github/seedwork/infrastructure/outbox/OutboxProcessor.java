package com.github.seedwork.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class OutboxProcessor implements SchedulingConfigurer {

  private final Logger LOGGER = LoggerFactory.getLogger(OutboxProcessor.class);

  private final OutboxProperties properties;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final LockRegistry lockRegistry;
  private final MessageStore messageStore;

  public OutboxProcessor(final OutboxProperties properties,
                         final ApplicationEventPublisher applicationEventPublisher,
                         final LockRegistry lockRegistry,
                         final MessageStore messageStore) {
    this.properties = Objects.requireNonNull(properties);
    this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    this.lockRegistry = Objects.requireNonNull(lockRegistry);
    this.messageStore = Objects.requireNonNull(messageStore);
  }

  private void processMessagesLocked() {
    try {
      lockRegistry.executeLocked("outbox", properties.pollInterval(), this::processMessages);
    } catch (final InterruptedException | TimeoutException e) {
      LOGGER.debug("Failed to acquire lock", e);
    }
  }

  private void processMessages() {
    final var messages = messageStore.peekAll(0, properties.maxRequeueCount(), 0L, properties.maxPeekCount())
      .stream()
      .collect(Collectors.groupingBy(Message::correlationId, Collectors.toUnmodifiableList()))
      .values()
      .parallelStream()
      .flatMap(Collection::stream);

    messages.forEach(this::processMessage);
  }

  private void processMessage(final Message message) {
    LOGGER.info("Processing message (messageId={}, type={}, requeueCount={})", message.messageId(), message.type(), message.requeueCount());
    try {
      applicationEventPublisher.publishEvent(message.payload());
      dequeueMessage(message);
    } catch (final Exception e) {
      LOGGER.error("Failed to process message", e);

      requeueMessage(message);
    }
  }

  private void dequeueMessage(final Message message) {
    LOGGER.info("Dequeueing message (messageId={}, type={}, requeueCount={})", message.messageId(), message.type(), message.requeueCount());
    try {
      messageStore.dequeue(message);
    } catch (final Exception e) {
      LOGGER.error("Failed to dequeue message", e);
    }
  }

  private void requeueMessage(final Message message) {
    LOGGER.info("Requeueing message (messageId={}, type={}, requeueCount={})", message.messageId(), message.type(), message.requeueCount());
    try {
      final var requeueDelay = properties.initialRequeueDelay().multipliedBy(message.requeueCount() + 1);

      messageStore.requeue(message, requeueDelay);
    } catch (final Exception e) {
      LOGGER.error("Failed to requeue message", e);
    }
  }

  @Override
  public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
    taskRegistrar.addFixedRateTask(this::processMessagesLocked, properties.pollInterval());
  }
}
