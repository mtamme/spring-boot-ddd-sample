package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.domain.TestEvent;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaMessageProducerTest extends PersistenceTest {

  @Autowired
  private JpaMessageRepository messageRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void enqueueShouldReturnEnqueuedMessage() {
    // Arrange
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageProducer = new JpaMessageProducer(
      messageRepository,
      clock);

    // Act
    final var message = transactionTemplate.execute(ts -> {
      return messageProducer.enqueue("A", "TestEvent", new TestEvent());
    });

    // Assert
    assertNotNull(message);
    assertEquals(1, message.sequenceNumber());
    assertEquals("A", message.groupId());
    assertEquals(Instant.EPOCH, message.enqueuedAt());
    assertEquals(Instant.EPOCH, message.availableAt());
    assertNull(message.lockId());
    assertEquals(0, message.attemptCount());
    assertEquals("TestEvent", message.subject());
    assertInstanceOf(TestEvent.class, message.body());
  }
}
