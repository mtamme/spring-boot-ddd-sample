package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.web.outbox.representation.MessageSummary;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageMapperImpl implements MessageMapper {

  @Override
  public PeekMessageResponse toPeekMessageResponse(final Message message) {
    return new PeekMessageResponse()
      .sequenceNumber(message.sequenceNumber())
      .groupId(message.groupId())
      .enqueuedAt(message.enqueuedAt())
      .availableAt(message.availableAt())
      .deliveryCount(message.deliveryCount())
      .subject(message.subject());
  }

  @Override
  public List<MessageSummary> toMessageSummaries(final List<Message> messages) {
    return messages.stream()
      .map(m -> new MessageSummary()
        .sequenceNumber(m.sequenceNumber())
        .groupId(m.groupId())
        .enqueuedAt(m.enqueuedAt())
        .availableAt(m.availableAt())
        .deliveryCount(m.deliveryCount())
        .subject(m.subject()))
      .toList();
  }
}
