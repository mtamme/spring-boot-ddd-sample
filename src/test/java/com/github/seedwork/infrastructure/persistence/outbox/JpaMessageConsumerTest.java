package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.MessageException;
import com.github.seedwork.infrastructure.outbox.MessageFixture;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaMessageConsumerTest extends PersistenceTest {

  @Autowired
  private JpaMessageRepository messageRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void countWithNoMessagesShouldReturnMessageCounts() {
    // Arrange
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messageCounts = transactionTemplate.execute(ts -> {
      return messageConsumer.count();
    });

    // Assert
    assertNotNull(messageCounts);
    assertEquals(0, messageCounts.activeCount());
    assertEquals(0, messageCounts.failedCount());
    assertEquals(0, messageCounts.lockedCount());
    assertEquals(0, messageCounts.totalCount());
  }

  @Test
  void countWithActiveMessageShouldReturnMessageCountsWithActiveCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messageCounts = transactionTemplate.execute(ts -> {
      return messageConsumer.count();
    });

    // Assert
    assertNotNull(messageCounts);
    assertEquals(1, messageCounts.activeCount());
    assertEquals(0, messageCounts.failedCount());
    assertEquals(0, messageCounts.lockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void countWithActiveLockedMessageShouldReturnMessageCountsWithActiveCountAndLockedCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messageCounts = transactionTemplate.execute(ts -> {
      return messageConsumer.count();
    });

    // Assert
    assertNotNull(messageCounts);
    assertEquals(1, messageCounts.activeCount());
    assertEquals(0, messageCounts.failedCount());
    assertEquals(1, messageCounts.lockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void countWithFailedMessageShouldReturnMessageCountsWithFailedCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messageCounts = transactionTemplate.execute(ts -> {
      return messageConsumer.count();
    });

    // Assert
    assertNotNull(messageCounts);
    assertEquals(0, messageCounts.activeCount());
    assertEquals(1, messageCounts.failedCount());
    assertEquals(0, messageCounts.lockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void countWithFailedLockedMessageShouldReturnMessageCountsWithFailedCountAndLockedCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messageCounts = transactionTemplate.execute(ts -> {
      return messageConsumer.count();
    });

    // Assert
    assertNotNull(messageCounts);
    assertEquals(0, messageCounts.activeCount());
    assertEquals(1, messageCounts.failedCount());
    assertEquals(1, messageCounts.lockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void peekShouldReturnMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var message = transactionTemplate.execute(ts -> {
      return messageConsumer.peek(1L);
    });

    // Assert
    assertNotNull(message);
    assertEquals(1L, message.sequenceNumber());
    assertEquals("A", message.groupId());
    assertEquals(Instant.EPOCH, message.enqueuedAt());
    assertEquals(Instant.EPOCH, message.availableAt());
    assertNull(message.lockId());
    assertEquals(0, message.attemptCount());
  }

  @Test
  void peekAllShouldReturnMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.peekAll(0L, 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getFirst().availableAt());
    assertNull(messages.getFirst().lockId());
    assertEquals(0, messages.getFirst().attemptCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("B", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getLast().availableAt());
    assertNull(messages.getLast().lockId());
    assertEquals(10, messages.getLast().attemptCount());
  }

  @Test
  void lockAllNextActiveWithActiveGroupShouldReturnNextActiveGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextActiveWithActiveLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithFailedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithFailedLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithActiveGroupAndActiveGroupShouldReturnNextActiveGroupMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().attemptCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("B", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getLast().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getLast().lockId());
    assertEquals(1, messages.getLast().attemptCount());
  }

  @Test
  void lockAllNextActiveWithActiveGroupAndActiveLockedGroupShouldReturnNextActiveGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextActiveWithActiveGroupAndFailedGroupShouldReturnNextActiveGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextActiveWithActiveGroupAndFailedLockedGroupShouldReturnNextActiveGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextActiveWithActiveLockedGroupAndActiveLockedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithActiveLockedGroupAndFailedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithActiveLockedGroupAndFailedLockedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithFailedGroupAndFailedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithFailedGroupAndFailedLockedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextActiveWithFailedLockedGroupAndFailedLockedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextActive(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithActiveGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithActiveLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithFailedGroupShouldReturnNextFailedGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(10, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextFailedWithFailedLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithActiveGroupAndActiveGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithActiveGroupAndActiveLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithActiveGroupAndFailedGroupShouldReturnNextFailedGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(2L, messages.getFirst().sequenceNumber());
    assertEquals("B", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(10, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextFailedWithActiveGroupAndFailedLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithActiveLockedGroupAndActiveLockedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithActiveLockedGroupAndFailedGroupShouldReturnNextFailedGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(2L, messages.getFirst().sequenceNumber());
    assertEquals("B", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(10, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextFailedWithActiveLockedGroupAndFailedLockedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllNextFailedWithFailedGroupAndFailedGroupShouldReturnNextFailedGroupMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(10, messages.getFirst().attemptCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("B", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getLast().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getLast().lockId());
    assertEquals(10, messages.getLast().attemptCount());
  }

  @Test
  void lockAllNextFailedWithFailedGroupAndFailedLockedGroupShouldReturnNextFailedGroupMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(10, messages.getFirst().attemptCount());
  }

  @Test
  void lockAllNextFailedWithFailedLockedGroupAndFailedLockedGroupShouldReturnNoMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllNextFailed(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void requeueLockedWithActiveMessageShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.requeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueLockedWithActiveLockedMessageShouldRequeueLockedMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getFirst().availableAt());
    assertNull(messages.getFirst().lockId());
    assertEquals(0, messages.getFirst().attemptCount());
  }

  @Test
  void requeueLockedWithFailedMessageShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.requeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueLockedWithFailedLockedMessageShouldRequeueLockedMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getFirst().availableAt());
    assertNull(messages.getFirst().lockId());
    assertEquals(0, messages.getFirst().attemptCount());
  }

  @Test
  void requeueAllLockedWithActiveMessagesShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.requeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueAllLockedWithActiveLockedMessagesShouldRequeueLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getFirst().availableAt());
    assertNull(messages.getFirst().lockId());
    assertEquals(0, messages.getFirst().attemptCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("B", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getLast().availableAt());
    assertNull(messages.getLast().lockId());
    assertEquals(0, messages.getLast().attemptCount());
  }

  @Test
  void requeueAllLockedWithFailedMessagesShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.requeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueAllLockedWithFailedLockedMessagesShouldRequeueLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getFirst().availableAt());
    assertNull(messages.getFirst().lockId());
    assertEquals(0, messages.getFirst().attemptCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("B", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getLast().availableAt());
    assertNull(messages.getLast().lockId());
    assertEquals(0, messages.getLast().attemptCount());
  }

  @Test
  void dequeueLockedWithActiveMessageShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.dequeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueLockedWithActiveLockedMessageShouldDequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void dequeueLockedWithFailedMessageShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.dequeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueLockedWithFailedLockedMessageShouldDequeueLockedMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeueLocked(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void dequeueAllLockedWithActiveMessagesShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.dequeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueAllLockedWithActiveLockedMessagesShouldDequeueLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void dequeueAllLockedWithFailedMessagesShouldThrowMessageException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageException.class, () -> messageConsumer.dequeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueAllLockedWithFailedLockedMessagesShouldDequeueLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.enqueue(MessageFixture.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
      messageRepository.enqueue(MessageFixture.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = mock(Clock.class);

    when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeueAllLocked(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }
}
