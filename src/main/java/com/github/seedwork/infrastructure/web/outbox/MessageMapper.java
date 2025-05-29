package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.outbox.MessageConsumer;
import com.github.seedwork.infrastructure.web.outbox.representation.LockNextMessagesResponse;
import com.github.seedwork.infrastructure.web.outbox.representation.MessageSummary;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessageResponse;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessagesResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnBean(MessageConsumer.class)
public class MessageMapper {

  private List<MessageSummary> toMessageSummaries(final List<Message> messages) {
    return messages.stream()
      .map(m -> new MessageSummary()
        .sequenceNumber(m.sequenceNumber())
        .groupId(m.groupId())
        .enqueuedAt(m.enqueuedAt())
        .availableAt(m.availableAt())
        .attemptCount(m.attemptCount())
        .subject(m.subject()))
      .toList();
  }

  public PeekMessageResponse toPeekMessageResponse(final Message message) {
    return new PeekMessageResponse()
      .sequenceNumber(message.sequenceNumber())
      .groupId(message.groupId())
      .enqueuedAt(message.enqueuedAt())
      .availableAt(message.availableAt())
      .attemptCount(message.attemptCount())
      .subject(message.subject());
  }

  public PeekMessagesResponse toPeekMessagesResponse(final List<Message> messages) {
    return new PeekMessagesResponse()
      .messages(toMessageSummaries(messages));
  }

  public LockNextMessagesResponse toLockNextMessagesResponse(final UUID lockId, final List<Message> messages) {
    return new LockNextMessagesResponse()
      .lockId(lockId)
      .messages(toMessageSummaries(messages));
  }
}
