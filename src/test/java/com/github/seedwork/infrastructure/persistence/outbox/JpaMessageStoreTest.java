package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.MessageNotFoundException;
import com.github.seedwork.infrastructure.outbox.Messages;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JpaMessageStoreTest extends PersistenceTest {

  @Autowired
  private JpaMessageRepository messageRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  public void countShouldReturnTotalMessageCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ofSeconds(2L),
        2,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000002",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(1L),
        Duration.ofSeconds(1L),
        1,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000003",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(2L),
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var count = transactionTemplate.execute(ts -> {
      return messageStore.count(0, Integer.MAX_VALUE);
    });

    // Assert
    assertEquals(3, count);
  }

  @Test
  public void countWithMaxRequeueCountShouldReturnQueuedMessageCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ofSeconds(2L),
        2,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000002",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(1L),
        Duration.ofSeconds(1L),
        1,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000003",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(2L),
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.ofEpochSecond(2L));
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var count = transactionTemplate.execute(ts -> {
      return messageStore.count(0, 1);
    });

    // Assert
    assertEquals(2, count);
  }

  @Test
  public void countWithMinRequeueCountShouldReturnPoisonMessageCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ofSeconds(2L),
        2,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000002",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(1L),
        Duration.ofSeconds(1L),
        1,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000003",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(2L),
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var count = transactionTemplate.execute(ts -> {
      return messageStore.count(2, Integer.MAX_VALUE);
    });

    // Assert
    assertEquals(1, count);
  }

  @Test
  public void peekShouldReturnMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var message = transactionTemplate.execute(ts -> {
      return messageStore.peek(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    });

    // Assert
    assertNotNull(message);
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), message.messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", message.correlationId());
    assertEquals("TestPayload", message.type());
    assertEquals(Instant.EPOCH, message.enqueuedAt());
    assertEquals(Instant.EPOCH, message.scheduledAt());
    assertEquals(Duration.ZERO, message.delay());
    assertEquals(0, message.requeueCount());
    assertInstanceOf(TestPayload.class, message.payload());
  }

  @Test
  public void peekWithUnknownMessageIdShouldThrowMessageNotFoundException() {
    // Arrange
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    // Assert
    assertThrows(MessageNotFoundException.class, () -> {
      transactionTemplate.executeWithoutResult(ts -> {
        messageStore.peek(UUID.fromString("00000000-0000-0000-0000-000000000001"));
      });
    });
  }

  @Test
  public void peekAllShouldReturnMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ofSeconds(2L),
        2,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000002",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(1L),
        Duration.ofSeconds(1L),
        1,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000003",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(2L),
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageStore.peekAll(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(3, messages.size());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), messages.getFirst().messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", messages.getFirst().correlationId());
    assertEquals("TestPayload", messages.getFirst().type());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.ofEpochSecond(2L), messages.getFirst().scheduledAt());
    assertEquals(Duration.ofSeconds(2L), messages.getFirst().delay());
    assertEquals(2, messages.getFirst().requeueCount());
    assertInstanceOf(TestPayload.class, messages.getFirst().payload());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000002"), messages.get(1).messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", messages.get(1).correlationId());
    assertEquals("TestPayload", messages.get(1).type());
    assertEquals(Instant.ofEpochSecond(1L), messages.get(1).enqueuedAt());
    assertEquals(Instant.ofEpochSecond(2L), messages.get(1).scheduledAt());
    assertEquals(Duration.ofSeconds(1L), messages.get(1).delay());
    assertEquals(1, messages.get(1).requeueCount());
    assertInstanceOf(TestPayload.class, messages.get(1).payload());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000003"), messages.get(2).messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", messages.get(2).correlationId());
    assertEquals("TestPayload", messages.get(2).type());
    assertEquals(Instant.ofEpochSecond(2L), messages.get(2).enqueuedAt());
    assertEquals(Instant.ofEpochSecond(2L), messages.get(2).scheduledAt());
    assertEquals(Duration.ZERO, messages.get(2).delay());
    assertEquals(0, messages.get(2).requeueCount());
    assertInstanceOf(TestPayload.class, messages.get(2).payload());
  }

  @Test
  public void peekAllWithMaxRequeueCountShouldReturnQueuedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ofSeconds(2L),
        2,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000002",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(1L),
        Duration.ofSeconds(1L),
        1,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000003",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(2L),
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.ofEpochSecond(2L));
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageStore.peekAll(0, 1, 0L, Integer.MAX_VALUE);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000002"), messages.getFirst().messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", messages.getFirst().correlationId());
    assertEquals("TestPayload", messages.getFirst().type());
    assertEquals(Instant.ofEpochSecond(1L), messages.getFirst().enqueuedAt());
    assertEquals(Instant.ofEpochSecond(2L), messages.getFirst().scheduledAt());
    assertEquals(Duration.ofSeconds(1L), messages.getFirst().delay());
    assertEquals(1, messages.getFirst().requeueCount());
    assertInstanceOf(TestPayload.class, messages.getFirst().payload());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000003"), messages.get(1).messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", messages.get(1).correlationId());
    assertEquals("TestPayload", messages.get(1).type());
    assertEquals(Instant.ofEpochSecond(2L), messages.get(1).enqueuedAt());
    assertEquals(Instant.ofEpochSecond(2L), messages.get(1).scheduledAt());
    assertEquals(Duration.ZERO, messages.get(1).delay());
    assertEquals(0, messages.get(1).requeueCount());
    assertInstanceOf(TestPayload.class, messages.get(1).payload());
  }

  @Test
  public void peekAllWithMinRequeueCountShouldReturnPoisonMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ofSeconds(2L),
        2,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000002",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(1L),
        Duration.ofSeconds(1L),
        1,
        new TestPayload()));
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000003",
        "00000000-0000-0000-0000-000000000000",
        Instant.ofEpochSecond(2L),
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageStore.peekAll(2, Integer.MAX_VALUE, 0L, Integer.MAX_VALUE);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), messages.getFirst().messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", messages.getFirst().correlationId());
    assertEquals("TestPayload", messages.getFirst().type());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.ofEpochSecond(2L), messages.getFirst().scheduledAt());
    assertEquals(Duration.ofSeconds(2L), messages.getFirst().delay());
    assertEquals(2, messages.getFirst().requeueCount());
    assertInstanceOf(TestPayload.class, messages.getFirst().payload());
  }

  @Test
  public void dequeueShouldDequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageStore.dequeue(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ZERO,
        0,
        new TestPayload()));
    });

    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var count = messageStore.count(0, Integer.MAX_VALUE);

      assertEquals(0, count);
    });
  }

  @Test
  public void dequeueWithUnknownMessageIdShouldThrowMessageNotFoundException() {
    // Arrange
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    // Assert
    assertThrows(MessageNotFoundException.class, () -> {
      transactionTemplate.executeWithoutResult(ts -> {
        messageStore.dequeue(Messages.newMessage(
          "00000000-0000-0000-0000-000000000001",
          "00000000-0000-0000-0000-000000000000",
          Instant.EPOCH,
          Duration.ZERO,
          0,
          new TestPayload()));
      });
    });
  }

  @Test
  public void enqueueShouldEnqueueMessage() {
    // Arrange
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    final var message = transactionTemplate.execute(ts -> {
      return messageStore.enqueue("00000000-0000-0000-0000-000000000000", new TestPayload());
    });

    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var messages = messageStore.peekAll(0, Integer.MAX_VALUE, 0L, 1);

      assertEquals(1, messages.size());
      assertInstanceOf(UUID.class, messages.getFirst().messageId());
      assertEquals("00000000-0000-0000-0000-000000000000", messages.getFirst().correlationId());
      assertEquals("TestPayload", messages.getFirst().type());
      assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
      assertEquals(Instant.EPOCH, messages.getFirst().scheduledAt());
      assertEquals(Duration.ZERO, messages.getFirst().delay());
      assertEquals(0, messages.getFirst().requeueCount());
      assertInstanceOf(TestPayload.class, messages.getFirst().payload());
    });
    assertNotNull(message);
    assertInstanceOf(UUID.class, message.messageId());
    assertEquals("00000000-0000-0000-0000-000000000000", message.correlationId());
    assertEquals("TestPayload", message.type());
    assertEquals(Instant.EPOCH, message.enqueuedAt());
    assertEquals(Instant.EPOCH, message.scheduledAt());
    assertEquals(Duration.ZERO, message.delay());
    assertEquals(0, message.requeueCount());
    assertInstanceOf(TestPayload.class, message.payload());
  }

  @Test
  public void requeueShouldRequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ZERO,
        1,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.ofEpochSecond(1L));
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageStore.requeue(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ZERO,
        1,
        new TestPayload()));
    });

    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var message = messageStore.peek(UUID.fromString("00000000-0000-0000-0000-000000000001"));

      assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), message.messageId());
      assertEquals("00000000-0000-0000-0000-000000000000", message.correlationId());
      assertEquals("TestPayload", message.type());
      assertEquals(Instant.EPOCH, message.enqueuedAt());
      assertEquals(Instant.ofEpochSecond(1L), message.scheduledAt());
      assertEquals(Duration.ofSeconds(1L), message.delay());
      assertEquals(0, message.requeueCount());
      assertInstanceOf(TestPayload.class, message.payload());
    });
  }

  @Test
  public void requeueWithUnknownMessageIdShouldThrowMessageNotFoundException() {
    // Arrange
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.ofEpochSecond(1L));
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    // Assert
    assertThrows(MessageNotFoundException.class, () -> {
      transactionTemplate.executeWithoutResult(ts -> {
        messageStore.requeue(Messages.newMessage(
          "00000000-0000-0000-0000-000000000001",
          "00000000-0000-0000-0000-000000000000",
          Instant.EPOCH,
          Duration.ZERO,
          1,
          new TestPayload()));
      });
    });
  }

  @Test
  public void requeueWithRequeueDelayShouldRequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000000",
        Instant.EPOCH,
        Duration.ZERO,
        0,
        new TestPayload()));
    });
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageStore.requeue(Messages.newMessage(
          "00000000-0000-0000-0000-000000000001",
          "00000000-0000-0000-0000-000000000000",
          Instant.EPOCH,
          Duration.ZERO,
          0,
          new TestPayload()),
        Duration.ofSeconds(1L));
    });

    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var message = messageStore.peek(UUID.fromString("00000000-0000-0000-0000-000000000001"));

      assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), message.messageId());
      assertEquals("00000000-0000-0000-0000-000000000000", message.correlationId());
      assertEquals("TestPayload", message.type());
      assertEquals(Instant.EPOCH, message.enqueuedAt());
      assertEquals(Instant.ofEpochSecond(1L), message.scheduledAt());
      assertEquals(Duration.ofSeconds(1L), message.delay());
      assertEquals(1, message.requeueCount());
      assertInstanceOf(TestPayload.class, message.payload());
    });
  }

  @Test
  public void requeueWithRequeueDelayAndUnknownMessageIdShouldThrowMessageNotFoundException() {
    // Arrange
    final var clock = Mockito.mock(Clock.class);
    final var messageStore = new JpaMessageStore(clock, messageRepository);

    // Act
    // Assert
    assertThrows(MessageNotFoundException.class, () -> {
      transactionTemplate.executeWithoutResult(ts -> {
        messageStore.requeue(Messages.newMessage(
            "00000000-0000-0000-0000-000000000001",
            "00000000-0000-0000-0000-000000000000",
            Instant.EPOCH,
            Duration.ZERO,
            0,
            new TestPayload()),
          Duration.ofSeconds(1L));
      });
    });
  }
}
