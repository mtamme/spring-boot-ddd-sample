package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.TestEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

public class OutboxEventPublisherTest {

  @Test
  void publishEventShouldEnqueueMessage() {
    // Arrange
    final var messageProducer = mock(MessageProducer.class);

    when(messageProducer.enqueue(anyString(), anyString(), any()))
      .thenReturn(MessageFixture.newMessage(
        1L,
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0,
        "TestEvent",
        new TestEvent()));
    final var outboxEventPublisher = new OutboxEventPublisher(messageProducer);

    // Act
    outboxEventPublisher.publishEvent("A", new TestEvent());

    // Assert
    verify(messageProducer, times(1))
      .enqueue(eq("A"), eq("TestEvent"), isA(TestEvent.class));
  }
}
