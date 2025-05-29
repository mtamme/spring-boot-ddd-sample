package com.github.seedwork.infrastructure.outbox;

import java.util.List;
import java.util.UUID;

public interface MessageConsumer {

  MessageCounts count();

  Message peek(Long sequenceNumber);

  List<Message> peekAll(long offset, int limit);

  List<Message> lockAllDeliverable(UUID lockId, int limit);

  List<Message> lockAllUndeliverable(UUID lockId, int limit);

  void requeue(Long sequenceNumber, UUID lockId);

  void requeueAll(List<Long> sequenceNumbers, UUID lockId);

  void dequeue(Long sequenceNumber, UUID lockId);

  void dequeueAll(List<Long> sequenceNumbers, UUID lockId);
}
