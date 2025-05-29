package com.github.seedwork.infrastructure.outbox;

import java.io.Serializable;

public interface MessageProducer {

  Message enqueue(String groupId, String subject, Serializable body);
}
