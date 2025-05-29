package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.Event;

@FunctionalInterface
public interface EventPublisher {

  void publishEvent(String groupId, Event event);
}
