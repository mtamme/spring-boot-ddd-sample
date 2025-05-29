package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.TestEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;

public class OutboxEventDispatcherTest {

  @Test
  void dispatchEventShouldEnqueueMessage() {
    // Arrange
    final var messageProducer = Mockito.mock(MessageProducer.class);

    Mockito.when(messageProducer.enqueue(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
      .thenReturn(Messages.newMessage(
        1L,
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0,
        "TestEvent",
        new TestEvent()));
    final var outboxEventDispatcher = new OutboxEventDispatcher(messageProducer);

    // Act
    outboxEventDispatcher.dispatchEvent("A", new TestEvent());

    // Assert
    Mockito.verify(messageProducer, Mockito.times(1))
      .enqueue(Mockito.eq("A"), Mockito.eq("TestEvent"), Mockito.isA(TestEvent.class));
  }
}
