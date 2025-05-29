package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;

public interface JpaMessageEnqueuer {

  void enqueue(Message message);
}
