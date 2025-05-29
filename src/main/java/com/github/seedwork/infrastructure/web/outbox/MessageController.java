package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.MessageConsumer;
import com.github.seedwork.infrastructure.web.outbox.representation.LockNextMessagesResponse;
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

  private final MessageConsumer messageConsumer;
  private final MessageMapper messageMapper;

  public MessageController(final MessageConsumer messageConsumer, final MessageMapper messageMapper) {
    this.messageConsumer = Objects.requireNonNull(messageConsumer);
    this.messageMapper = Objects.requireNonNull(messageMapper);
  }

  @Override
  public ResponseEntity<PeekMessageResponse> peekMessage(final Long sequenceNumber) {
    final var message = messageConsumer.peek(sequenceNumber);
    final var body = messageMapper.toPeekMessageResponse(message);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<PeekMessagesResponse> peekMessages(final Long offset, final Integer limit) {
    final var messages = messageConsumer.peekAll(offset, limit);
    final var body = messageMapper.toPeekMessagesResponse(messages);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<LockNextMessagesResponse> lockNextMessages(final Integer limit) {
    final var lockId = UUID.randomUUID();
    final var messages = messageConsumer.lockAllNextFailed(lockId, limit);
    final var body = messageMapper.toLockNextMessagesResponse(lockId, messages);

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(body);
  }

  @Override
  public ResponseEntity<Void> requeueLockedMessage(final Long sequenceNumber, final UUID lockId) {
    messageConsumer.requeueLocked(sequenceNumber, lockId);

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> dequeueLockedMessage(final Long sequenceNumber, final UUID lockId) {
    messageConsumer.dequeueLocked(sequenceNumber, lockId);

    return ResponseEntity.noContent()
      .build();
  }
}
