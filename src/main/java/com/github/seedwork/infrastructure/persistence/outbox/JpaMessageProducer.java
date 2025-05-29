package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.outbox.MessageProducer;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Clock;
import java.util.Objects;

@Transactional
public class JpaMessageProducer implements MessageProducer {

  private final JpaMessageRepository repository;
  private final Clock clock;

  public JpaMessageProducer(final JpaMessageRepository repository, final Clock clock) {
    this.repository = Objects.requireNonNull(repository);
    this.clock = Objects.requireNonNull(clock);
  }

  @Override
  public Message enqueue(final String groupId, final String subject, final Serializable body) {
    final var message = new Message(
      groupId,
      clock.instant(),
      subject,
      body);

    repository.persist(message);

    return message;
  }
}
