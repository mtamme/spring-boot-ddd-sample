package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.TestEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

    Mockito.when(messageConsumer.lockAllNextDeliverable(Mockito.any(), Mockito.anyInt()))
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
      .lockAllNextDeliverable(Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), Mockito.eq(100));
    Mockito.verify(messageConsumer, Mockito.never())
      .dequeueAllLocked(Mockito.any(), Mockito.any());
    Mockito.verify(eventPublisher, Mockito.never())
      .publishEvent(Mockito.any(Object.class));
  }

  @Test
  void runWithDeliverableMessagesShouldDequeueAllMessages() {
    // Arrange
    final var messageConsumer = Mockito.mock(MessageConsumer.class);

    Mockito.when(messageConsumer.lockAllNextDeliverable(Mockito.any(), Mockito.anyInt()))
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
      .lockAllNextDeliverable(Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), Mockito.eq(100));
    Mockito.verify(messageConsumer, Mockito.times(1))
      .dequeueAllLocked(Mockito.eq(List.of(1L, 2L)), Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    Mockito.verify(eventPublisher, Mockito.times(2))
      .publishEvent(Mockito.any(Object.class));
  }

  @Test
  void runWithDeliverableMessagesAndUnprocessableMessageShouldNotDequeueUnprocessableMessage() {
    // Arrange
    final var messageConsumer = Mockito.mock(MessageConsumer.class);

    Mockito.when(messageConsumer.lockAllNextDeliverable(Mockito.any(), Mockito.anyInt()))
      .thenReturn(List.of(
        Messages.newUnprocessableMessage(
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
          1)));
    final var eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

    Mockito.doNothing()
      .when(eventPublisher)
      .publishEvent(ArgumentMatchers.<Object>assertArg(e -> {
        if ((e instanceof TestEvent(boolean processable)) && !processable) {
          throw new RuntimeException("An error occurred");
        }
      }));
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
      .lockAllNextDeliverable(Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), Mockito.eq(100));
    Mockito.verify(messageConsumer, Mockito.times(1))
      .dequeueAllLocked(Mockito.eq(List.of(2L)), Mockito.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    Mockito.verify(eventPublisher, Mockito.times(2))
      .publishEvent(Mockito.any(Object.class));
  }
}
