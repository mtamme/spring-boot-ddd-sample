package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.TestEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class OutboxPollerTest {

  @Test
  void runWithNoActiveMessagesShouldDoNothing() {
    // Arrange
    final var messageConsumer = mock(MessageConsumer.class);

    when(messageConsumer.lockAllNextActive(any(), anyInt()))
      .thenReturn(List.of());
    final var applicationEventPublisher = mock(ApplicationEventPublisher.class);
    final var outboxPoller = new OutboxPoller(
      OutboxPropertiesFixture.newOutboxProperties(),
      messageConsumer,
      applicationEventPublisher);

    // Act
    outboxPoller.run(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    // Assert
    verify(messageConsumer, times(1))
      .lockAllNextActive(eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), eq(100));
    verify(messageConsumer, never())
      .dequeueAllLocked(any(), any());
    verify(applicationEventPublisher, never())
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
    final var applicationEventPublisher = mock(ApplicationEventPublisher.class);
    final var outboxPoller = new OutboxPoller(
      OutboxPropertiesFixture.newOutboxProperties(),
      messageConsumer,
      applicationEventPublisher);

    // Act
    outboxPoller.run(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    // Assert
    verify(messageConsumer, times(1))
      .lockAllNextActive(eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), eq(100));
    verify(messageConsumer, times(1))
      .dequeueAllLocked(eq(List.of(1L, 2L)), eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    verify(applicationEventPublisher, times(2))
      .publishEvent(any(Object.class));
  }

  @Test
  void runWithActiveMessagesAndUndispatchableMessageShouldNotDequeueUndispatchableMessage() {
    // Arrange
    final var messageConsumer = mock(MessageConsumer.class);

    when(messageConsumer.lockAllNextActive(any(), anyInt()))
      .thenReturn(List.of(
        MessageFixture.newUndispatchableMessage(
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
    final var applicationEventPublisher = mock(ApplicationEventPublisher.class);

    doNothing()
      .when(applicationEventPublisher)
      .publishEvent(ArgumentMatchers.<Object>assertArg(e -> {
        if ((e instanceof TestEvent(boolean dispatchable)) && !dispatchable) {
          throw new RuntimeException("An error occurred");
        }
      }));
    final var outboxPoller = new OutboxPoller(
      OutboxPropertiesFixture.newOutboxProperties(),
      messageConsumer,
      applicationEventPublisher);

    // Act
    outboxPoller.run(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    // Assert
    verify(messageConsumer, times(1))
      .lockAllNextActive(eq(UUID.fromString("00000000-0000-0000-0000-000000000000")), eq(100));
    verify(messageConsumer, times(1))
      .dequeueAllLocked(eq(List.of(2L)), eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    verify(applicationEventPublisher, times(2))
      .publishEvent(any(Object.class));
  }
}
