package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.MessageConsumer;
import com.github.seedwork.infrastructure.web.outbox.representation.LockMessagesResponse;
import com.github.seedwork.infrastructure.web.outbox.representation.MessageSummary;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessageResponse;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessagesResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@ConditionalOnBean(MessageConsumer.class)
public class MessageController implements MessageOperations {

  private final MessageConsumer consumer;

  public MessageController(final MessageConsumer consumer) {
    this.consumer = Objects.requireNonNull(consumer);
  }

  @Override
  public ResponseEntity<PeekMessageResponse> peekMessage(final Long sequenceNumber) {
    final var message = consumer.peek(sequenceNumber);
    final var body = new PeekMessageResponse()
      .sequenceNumber(message.sequenceNumber())
      .groupId(message.groupId())
      .enqueuedAt(message.enqueuedAt())
      .availableAt(message.availableAt())
      .deliveryCount(message.deliveryCount())
      .subject(message.subject());

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<PeekMessagesResponse> peekMessages(final Long offset, final Integer limit) {
    final var messages = consumer.peekAll(offset, limit)
      .stream()
      .map(m -> new MessageSummary()
        .sequenceNumber(m.sequenceNumber())
        .groupId(m.groupId())
        .enqueuedAt(m.enqueuedAt())
        .availableAt(m.availableAt())
        .deliveryCount(m.deliveryCount())
        .subject(m.subject()))
      .toList();
    final var body = new PeekMessagesResponse()
      .messages(messages);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<LockMessagesResponse> lockMessages(final Integer limit) {
    final var lockId = UUID.randomUUID();
    final var messages = consumer.lockAllUndeliverable(lockId, limit)
      .stream()
      .map(m -> new MessageSummary()
        .sequenceNumber(m.sequenceNumber())
        .groupId(m.groupId())
        .enqueuedAt(m.enqueuedAt())
        .availableAt(m.availableAt())
        .deliveryCount(m.deliveryCount())
        .subject(m.subject()))
      .toList();
    final var body = new LockMessagesResponse()
      .lockId(lockId)
      .messages(messages);

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(body);
  }

  @Override
  public ResponseEntity<Void> requeueMessage(final Long sequenceNumber, final UUID lockId) {
    consumer.requeue(sequenceNumber, lockId);

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> dequeueMessage(final Long sequenceNumber, final UUID lockId) {
    consumer.dequeue(sequenceNumber, lockId);

    return ResponseEntity.noContent()
      .build();
  }
}
