package com.github.seedwork.infrastructure.outbox;

import java.util.List;
import java.util.UUID;

public interface MessageConsumer {

  MessageCounts count();

  Message peek(Long sequenceNumber);

  List<Message> peekAll(long offset, int limit);

  List<Message> lockAllNextActive(UUID lockId, int limit);

  List<Message> lockAllNextFailed(UUID lockId, int limit);

  void requeueLocked(Long sequenceNumber, UUID lockId);

  void requeueAllLocked(List<Long> sequenceNumbers, UUID lockId);

  void dequeueLocked(Long sequenceNumber, UUID lockId);

  void dequeueAllLocked(List<Long> sequenceNumbers, UUID lockId);
}
