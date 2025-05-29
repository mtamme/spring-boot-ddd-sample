package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.TestEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class OutboxProcessorTest {

  @Test
  void runWithNoActiveMessagesShouldDoNothing() {
    // Arrange
    final var messageConsumer = mock(MessageConsumer.class);

    when(messageConsumer.lockAllNextActive(any(), anyInt()))
      .thenReturn(List.of());
    final var eventPublisher = mock(ApplicationEventPublisher.class);
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
    verify(messageConsumer, times(1))
      .lockAllNextActive(eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), eq(100));
    verify(messageConsumer, never())
      .dequeueAllLocked(any(), any());
    verify(eventPublisher, never())
      .publishEvent(any(Object.class));
  }

  @Test
  void runWithActiveMessagesShouldDequeueAllMessages() {
    // Arrange
    final var messageConsumer = mock(MessageConsumer.class);

    when(messageConsumer.lockAllNextActive(any(), anyInt()))
      .thenReturn(List.of(
        MessageFixture.newMessage(
          1L,
          "A",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        MessageFixture.newMessage(
          2L,
          "B",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1)));
    final var eventPublisher = mock(ApplicationEventPublisher.class);
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
    verify(messageConsumer, times(1))
      .lockAllNextActive(eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), eq(100));
    verify(messageConsumer, times(1))
      .dequeueAllLocked(eq(List.of(1L, 2L)), eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    verify(eventPublisher, times(2))
      .publishEvent(any(Object.class));
  }

  @Test
  void runWithActiveMessagesAndUnprocessableMessageShouldNotDequeueUnprocessableMessage() {
    // Arrange
    final var messageConsumer = mock(MessageConsumer.class);

    when(messageConsumer.lockAllNextActive(any(), anyInt()))
      .thenReturn(List.of(
        MessageFixture.newUnprocessableMessage(
          1L,
          "A",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1),
        MessageFixture.newMessage(
          2L,
          "B",
          Instant.EPOCH,
          Instant.EPOCH,
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          1)));
    final var eventPublisher = mock(ApplicationEventPublisher.class);

    doNothing()
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
    verify(messageConsumer, times(1))
      .lockAllNextActive(eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), eq(100));
    verify(messageConsumer, times(1))
      .dequeueAllLocked(eq(List.of(2L)), eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    verify(eventPublisher, times(2))
      .publishEvent(any(Object.class));
  }
}
