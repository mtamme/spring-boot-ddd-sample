package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.outbox.MessageNotFoundException;
import com.github.seedwork.infrastructure.outbox.MessageStore;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Transactional
public class JpaMessageStore implements MessageStore {

  private static final Instant MAX_SCHEDULED_AT = Instant.ofEpochMilli(253402300799999L);

  private final Clock clock;
  private final JpaMessageRepository messageRepository;

  public JpaMessageStore(final Clock clock, final JpaMessageRepository messageRepository) {
    this.clock = Objects.requireNonNull(clock);
    this.messageRepository = Objects.requireNonNull(messageRepository);
  }

  @Override
  public int count(final int minRequeueCount, final int maxRequeueCount) {
    final var scheduledAt = (maxRequeueCount < Integer.MAX_VALUE) ? clock.instant() : MAX_SCHEDULED_AT;

    return messageRepository.count(scheduledAt, minRequeueCount, maxRequeueCount);
  }

  @Override
  public Message peek(final UUID messageId) {
    return messageRepository.find(messageId)
      .orElseThrow(MessageNotFoundException::new);
  }

  @Override
  public List<Message> peekAll(final int minRequeueCount, final int maxRequeueCount, final long offset, final int limit) {
    final var scheduledAt = (maxRequeueCount < Integer.MAX_VALUE) ? clock.instant() : MAX_SCHEDULED_AT;

    return messageRepository.findAll(scheduledAt, minRequeueCount, maxRequeueCount, offset, limit);
  }

  @Override
  public void dequeue(final Message message) {
    final var count = messageRepository.delete(message.messageId());

    if (count == 0) {
      throw new MessageNotFoundException();
    }
  }

  @Override
  public void enqueue(final Message message) {
    messageRepository.persist(message);
  }

  @Override
  public void requeue(final Message message) {
    final var count = messageRepository.update(
      message.messageId(),
      clock.instant(),
      0);

    if (count == 0) {
      throw new MessageNotFoundException();
    }
  }

  @Override
  public void requeue(final Message message, final Duration requeueDelay) {
    final var count = messageRepository.update(
      message.messageId(),
      message.nextScheduledAt(requeueDelay),
      message.nextRequeueCount());

    if (count == 0) {
      throw new MessageNotFoundException();
    }
  }
}
