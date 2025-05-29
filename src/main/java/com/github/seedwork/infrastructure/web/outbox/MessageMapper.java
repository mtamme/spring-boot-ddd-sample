package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.web.outbox.representation.LockNextMessagesResponse;
import com.github.seedwork.infrastructure.web.outbox.representation.MessageSummary;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessageResponse;
import com.github.seedwork.infrastructure.web.outbox.representation.PeekMessagesResponse;

import java.util.List;
import java.util.UUID;

public interface MessageMapper {

  PeekMessageResponse toPeekMessageResponse(Message message);

  default PeekMessagesResponse toPeekMessagesResponse(final List<Message> messages) {
    return new PeekMessagesResponse()
      .messages(toMessageSummaries(messages));
  }

  default LockNextMessagesResponse toLockNextMessagesResponse(final UUID lockId, final List<Message> messages) {
    return new LockNextMessagesResponse()
      .lockId(lockId)
      .messages(toMessageSummaries(messages));
  }

  List<MessageSummary> toMessageSummaries(List<Message> messages);
}
