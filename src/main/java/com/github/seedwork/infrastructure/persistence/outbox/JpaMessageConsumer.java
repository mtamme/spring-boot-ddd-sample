package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.outbox.MessageConsumer;
import com.github.seedwork.infrastructure.outbox.MessageCounts;
import com.github.seedwork.infrastructure.outbox.MessageException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Transactional
public class JpaMessageConsumer implements MessageConsumer {

  private final JpaMessageRepository repository;
  private final Clock clock;
  private final Duration lockDuration;
  private final int maxAttemptCount;

  public JpaMessageConsumer(final JpaMessageRepository repository,
                            final Clock clock,
                            final Duration lockDuration,
                            final int maxAttemptCount) {
    this.repository = Objects.requireNonNull(repository);
    this.clock = Objects.requireNonNull(clock);
    this.lockDuration = Objects.requireNonNull(lockDuration);
    this.maxAttemptCount = maxAttemptCount;
  }

  @Override
  public MessageCounts count() {
    return repository.count(maxAttemptCount, clock.instant());
  }

  @Override
  public Message peek(final Long sequenceNumber) {
    return repository.peek(sequenceNumber)
      .orElseThrow(MessageException::notFound);
  }

  @Override
  public List<Message> peekAll(final long offset, final int limit) {
    return repository.peekAll(offset, limit);
  }

  @Override
  public List<Message> lockAllNextActive(final UUID lockId, final int limit) {
    final var lockedAt = clock.instant();
    final var lockCount = repository.lockAllNextActive(
      lockedAt.plus(lockDuration),
      lockId,
      limit,
      maxAttemptCount,
      lockedAt);

    if (lockCount == 0) {
      return List.of();
    }

    return repository.peekAllLocked(lockId, clock.instant());
  }

  @Override
  public List<Message> lockAllNextFailed(final UUID lockId, final int limit) {
    final var lockedAt = clock.instant();
    final var lockCount = repository.lockAllNextFailed(
      lockedAt.plus(lockDuration),
      lockId,
      limit,
      maxAttemptCount,
      lockedAt);

    if (lockCount == 0) {
      return List.of();
    }

    return repository.peekAllLocked(lockId, clock.instant());
  }

  @Override
  public void requeueLocked(final Long sequenceNumber, final UUID lockId) {
    final var requeueCount = repository.requeueLocked(sequenceNumber, lockId, clock.instant());

    if (requeueCount != 1) {
      throw MessageException.notFound();
    }
  }

  @Override
  public void requeueAllLocked(final List<Long> sequenceNumbers, final UUID lockId) {
    if (sequenceNumbers.isEmpty()) {
      return;
    }
    final var requeueCount = repository.requeueAllLocked(sequenceNumbers, lockId, clock.instant());

    if (requeueCount != sequenceNumbers.size()) {
      throw MessageException.notFound();
    }
  }

  @Override
  public void dequeueLocked(final Long sequenceNumber, final UUID lockId) {
    final var dequeueCount = repository.dequeueLocked(sequenceNumber, lockId, clock.instant());

    if (dequeueCount != 1) {
      throw MessageException.notFound();
    }
  }

  @Override
  public void dequeueAllLocked(final List<Long> sequenceNumbers, final UUID lockId) {
    if (sequenceNumbers.isEmpty()) {
      return;
    }
    final var dequeueCount = repository.dequeueAllLocked(sequenceNumbers, lockId, clock.instant());

    if (dequeueCount != sequenceNumbers.size()) {
      throw MessageException.notFound();
    }
  }
}
