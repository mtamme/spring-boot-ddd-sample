package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.MessageStore;
import com.github.seedwork.infrastructure.web.outbox.representation.Message;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessagesResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@ConditionalOnBean(MessageStore.class)
@RestController
public class OutboxController implements OutboxOperations {

  private final MessageStore messageStore;

  public OutboxController(final MessageStore messageStore) {
    this.messageStore = Objects.requireNonNull(messageStore);
  }

  @Override
  public ResponseEntity<Message> peekMessage(final UUID messageId) {
    final var message = messageStore.peek(messageId);
    final var body = new Message()
      .messageId(message.messageId())
      .correlationId(message.correlationId())
      .type(message.type())
      .enqueuedAt(message.enqueuedAt())
      .scheduledAt(message.scheduledAt())
      .delay(message.delay().toString())
      .requeueCount(message.requeueCount());

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<PeekMessagesResponse> peekMessages(final Long offset, final Integer limit) {
    final var messages = messageStore.peekAll(0, Integer.MAX_VALUE, offset, limit)
      .stream()
      .map(m -> new Message()
        .messageId(m.messageId())
        .correlationId(m.correlationId())
        .type(m.type())
        .enqueuedAt(m.enqueuedAt())
        .scheduledAt(m.scheduledAt())
        .delay(m.delay().toString())
        .requeueCount(m.requeueCount()))
      .toList();
    final var body = new PeekMessagesResponse()
      .messages(messages);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<Void> dequeueMessage(final UUID messageId) {
    final var message = messageStore.peek(messageId);

    messageStore.dequeue(message);

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> requeueMessage(final UUID messageId) {
    final var message = messageStore.peek(messageId);

    messageStore.requeue(message);

    return ResponseEntity.noContent()
      .build();
  }
}
