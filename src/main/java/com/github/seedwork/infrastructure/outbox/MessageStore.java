package com.github.seedwork.infrastructure.outbox;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface MessageStore {

  int count(int minRequeueCount, int maxRequeueCount);

  Message peek(UUID messageId);

  List<Message> peekAll(int minRequeueCount,
                        int maxRequeueCount,
                        long offset,
                        int limit);

  Message enqueue(String correlationId, Serializable payload);

  void requeue(Message message);

  void requeue(Message message, Duration requeueDelay);

  void dequeue(Message message);
}
