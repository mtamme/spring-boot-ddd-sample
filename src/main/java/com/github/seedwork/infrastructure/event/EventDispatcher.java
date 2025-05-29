package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.Event;

@FunctionalInterface
public interface EventDispatcher {

  void dispatchEvent(Event event);
}
