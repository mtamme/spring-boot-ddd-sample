package com.github.seedwork.infrastructure.outbox;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OutboxProcessorTest {

  @Test
  void runWithNoDeliverableMessagesShouldDoNothing() {
    // Arrange
    final var messageConsumer = Mockito.mock(MessageConsumer.class);

    Mockito.when(messageConsumer.lockAllDeliverable(Mockito.any(), Mockito.anyInt()))
      .thenReturn(List.of());
    final var eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    final var outboxProcessor = new OutboxProcessor(
      new OutboxProperties(
        true,
        Duration.ofSeconds(1L),
        Duration.ofSeconds(30L),
        100,
        10),
      messageConsumer,
      eventPublisher);

    // Act
    outboxProcessor.run(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    // Assert
    Mockito.verify(messageConsumer, Mockito.times(1))
      .lockAllDeliverable(Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), Mockito.eq(100));
    Mockito.verify(messageConsumer, Mockito.never())
      .dequeueAll(Mockito.any(), Mockito.any());
    Mockito.verify(eventPublisher, Mockito.never())
      .publishEvent(Mockito.any(Object.class));
  }

  @Test
  void runWithDeliverableMessagesShouldProcessMessagesGrouped() {
    // Arrange
    final var messageConsumer = Mockito.mock(MessageConsumer.class);

    Mockito.when(messageConsumer.lockAllDeliverable(Mockito.any(), Mockito.anyInt()))
      .thenReturn(List.of(
        Messages.newMessage(
          1L,
          "A",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        Messages.newMessage(
          2L,
          "B",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        Messages.newMessage(
          3L,
          "A",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        Messages.newMessage(
          4L,
          "B",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1)));
    final var eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    final var outboxProcessor = new OutboxProcessor(
      new OutboxProperties(
        true,
        Duration.ofSeconds(1L),
        Duration.ofSeconds(30L),
        100,
        10),
      messageConsumer,
      eventPublisher);

    // Act
    outboxProcessor.run(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    // Assert
    Mockito.verify(messageConsumer, Mockito.times(1))
      .lockAllDeliverable(Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), Mockito.eq(100));
    Mockito.verify(messageConsumer, Mockito.times(1))
      .dequeueAll(Mockito.eq(List.of(1L, 3L, 2L, 4L)), Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    Mockito.verify(eventPublisher, Mockito.times(4))
      .publishEvent(Mockito.any(Object.class));
  }

  @Test
  void runWithDeliverableMessagesAndFailureShouldProcessMessagesGroupedUntilFailure() {
    // Arrange
    final var messageConsumer = Mockito.mock(MessageConsumer.class);

    Mockito.when(messageConsumer.lockAllDeliverable(Mockito.any(), Mockito.anyInt()))
      .thenReturn(List.of(
        Messages.newMessage(
          1L,
          "A",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        Messages.newMessage(
          2L,
          "B",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        Messages.newMessage(
          3L,
          "A",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        Messages.newMessage(
          4L,
          "B",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1)));
    final var eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

    Mockito.doThrow(new RuntimeException("An error occurred"))
      .doNothing()
      .when(eventPublisher)
      .publishEvent(Mockito.any(Object.class));
    final var outboxProcessor = new OutboxProcessor(
      new OutboxProperties(
        true,
        Duration.ofSeconds(1L),
        Duration.ofSeconds(30L),
        100,
        10),
      messageConsumer,
      eventPublisher);

    // Act
    outboxProcessor.run(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    // Assert
    Mockito.verify(messageConsumer, Mockito.times(1))
      .lockAllDeliverable(Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), Mockito.eq(100));
    Mockito.verify(messageConsumer, Mockito.times(1))
      .dequeueAll(Mockito.eq(List.of(2L, 4L)), Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    Mockito.verify(eventPublisher, Mockito.times(3))
      .publishEvent(Mockito.any(Object.class));
  }
}
