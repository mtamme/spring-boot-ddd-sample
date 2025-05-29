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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JpaMessageConsumerTest extends PersistenceTest {

  @Autowired
  private JpaMessageRepository messageRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void countWithNoMessagesShouldReturnMessageCounts() {
    // Arrange
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
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
    assertEquals(0, messageCounts.deliverableCount());
    assertEquals(0, messageCounts.deliverableLockedCount());
    assertEquals(0, messageCounts.undeliverableCount());
    assertEquals(0, messageCounts.undeliverableLockedCount());
    assertEquals(0, messageCounts.totalCount());
  }

  @Test
  void countWithDeliverableMessageShouldReturnMessageCountsWithDeliverableCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
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
    assertEquals(1, messageCounts.deliverableCount());
    assertEquals(0, messageCounts.deliverableLockedCount());
    assertEquals(0, messageCounts.undeliverableCount());
    assertEquals(0, messageCounts.undeliverableLockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void countWithDeliverableLockedMessageShouldReturnMessageCountsWithDeliverableLockedCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
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
    assertEquals(0, messageCounts.deliverableCount());
    assertEquals(1, messageCounts.deliverableLockedCount());
    assertEquals(0, messageCounts.undeliverableCount());
    assertEquals(0, messageCounts.undeliverableLockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void countWithUndeliverableMessageShouldReturnMessageCountsWithUndeliverableCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
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
    assertEquals(0, messageCounts.deliverableCount());
    assertEquals(0, messageCounts.deliverableLockedCount());
    assertEquals(1, messageCounts.undeliverableCount());
    assertEquals(0, messageCounts.undeliverableLockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void countWithUndeliverableLockedMessageShouldReturnMessageCountsWithUndeliverableLockedCount() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
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
    assertEquals(0, messageCounts.deliverableCount());
    assertEquals(0, messageCounts.deliverableLockedCount());
    assertEquals(0, messageCounts.undeliverableCount());
    assertEquals(1, messageCounts.undeliverableLockedCount());
    assertEquals(1, messageCounts.totalCount());
  }

  @Test
  void peekShouldReturnMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
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
    assertEquals(0, message.deliveryCount());
  }

  @Test
  void peekAllShouldReturnMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
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
    assertEquals(0, messages.getFirst().deliveryCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("B", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getLast().availableAt());
    assertNull(messages.getLast().lockId());
    assertEquals(10, messages.getLast().deliveryCount());
  }

  @Test
  void lockAllDeliverableWithDeliverableGroupShouldReturnDeliverableLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().deliveryCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("A", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getLast().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getLast().lockId());
    assertEquals(1, messages.getLast().deliveryCount());
  }

  @Test
  void lockAllDeliverableWithDeliverableLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllDeliverableWithUndeliverableGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllDeliverableWithUndeliverableLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllDeliverableWithDeliverableGroupAndDeliverableGroupShouldReturnDeliverableLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(2, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().deliveryCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("B", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getLast().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getLast().lockId());
    assertEquals(1, messages.getLast().deliveryCount());
  }

  @Test
  void lockAllDeliverableWithDeliverableGroupAndDeliverableLockedGroupShouldReturnDeliverableLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().deliveryCount());
  }

  @Test
  void lockAllDeliverableWithDeliverableGroupAndUndeliverableGroupShouldReturnDeliverableLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().deliveryCount());
  }

  @Test
  void lockAllDeliverableWithDeliverableGroupAndUndeliverableLockedGroupShouldReturnDeliverableLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllDeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(1, messages.getFirst().deliveryCount());
  }

  @Test
  void lockAllUndeliverableWithDeliverableGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllUndeliverableWithDeliverableLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllUndeliverableWithUndeliverableGroupShouldReturnUndeliverableLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(1L, messages.getFirst().sequenceNumber());
    assertEquals("A", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(10, messages.getFirst().deliveryCount());
  }

  @Test
  void lockAllUndeliverableWithUndeliverableLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllUndeliverableWithDeliverableGroupAndDeliverableGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllUndeliverableWithDeliverableGroupAndDeliverableLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void lockAllUndeliverableWithDeliverableGroupAndUndeliverableGroupShouldReturnUndeliverableLockedMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(1, messages.size());
    assertEquals(2L, messages.getFirst().sequenceNumber());
    assertEquals("B", messages.getFirst().groupId());
    assertEquals(Instant.EPOCH, messages.getFirst().enqueuedAt());
    assertEquals(Instant.EPOCH.plusSeconds(30L), messages.getFirst().availableAt());
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), messages.getFirst().lockId());
    assertEquals(10, messages.getFirst().deliveryCount());
  }

  @Test
  void lockAllUndeliverableWithDeliverableGroupAndUndeliverableLockedGroupShouldReturnNoMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "B",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    final var messages = transactionTemplate.execute(ts -> {
      return messageConsumer.lockAllUndeliverable(UUID.fromString("00000000-0000-0000-0000-000000000000"), 100);
    });

    // Assert
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void requeueWithDeliverableMessageShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.requeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueWithDeliverableLockedMessageShouldRequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
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
    assertEquals(0, messages.getFirst().deliveryCount());
  }

  @Test
  void requeueWithUndeliverableMessageShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.requeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueWithUndeliverableLockedMessageShouldRequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
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
    assertEquals(0, messages.getFirst().deliveryCount());
  }

  @Test
  void requeueAllWithDeliverableMessagesShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.requeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueAllWithDeliverableLockedMessagesShouldRequeueMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
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
    assertEquals(0, messages.getFirst().deliveryCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("A", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getLast().availableAt());
    assertNull(messages.getLast().lockId());
    assertEquals(0, messages.getLast().deliveryCount());
  }

  @Test
  void requeueAllWithUndeliverableMessagesShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.requeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void requeueAllWithUndeliverableLockedMessagesShouldRequeueMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.requeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
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
    assertEquals(0, messages.getFirst().deliveryCount());
    assertEquals(2L, messages.getLast().sequenceNumber());
    assertEquals("A", messages.getLast().groupId());
    assertEquals(Instant.EPOCH, messages.getLast().enqueuedAt());
    assertEquals(Instant.EPOCH, messages.getLast().availableAt());
    assertNull(messages.getLast().lockId());
    assertEquals(0, messages.getLast().deliveryCount());
  }

  @Test
  void dequeueWithDeliverableMessageShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.dequeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueWithDeliverableLockedMessageShouldDequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void dequeueWithUndeliverableMessageShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.dequeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueWithUndeliverableLockedMessageShouldDequeueMessage() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeue(1L, UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void dequeueAllWithDeliverableMessagesShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.dequeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueAllWithDeliverableLockedMessagesShouldDequeueMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        0));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @Test
  void dequeueAllWithUndeliverableMessagesShouldThrowMessageNotFoundException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      assertThrows(MessageNotFoundException.class, () -> messageConsumer.dequeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000")));
    });
  }

  @Test
  void dequeueAllWithUndeliverableLockedMessagesShouldDequeueMessages() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
      messageRepository.persist(Messages.newMessage(
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        10));
    });
    final var clock = Mockito.mock(Clock.class);

    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    final var messageConsumer = new JpaMessageConsumer(
      messageRepository,
      clock,
      Duration.ofSeconds(30L),
      10);

    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      messageConsumer.dequeueAll(List.of(1L, 2L), UUID.fromString("00000000-0000-0000-0000-000000000000"));
    });

    // Assert
    final var messages = transactionTemplate.execute(ts -> {
      return messageRepository.peekAll(0L, 100);
    });

    assertNotNull(messages);
    assertEquals(0, messages.size());
  }
}
