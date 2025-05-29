package com.github.seedwork.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AggregateRoot implements Entity {

  private Long version;
  private final List<Event> recordedEvents;

  protected AggregateRoot() {
    this.recordedEvents = new ArrayList<>();
  }

  public abstract String aggregateId();

  public List<Event> recordedEvents() {
    return List.copyOf(recordedEvents);
  }

  public void recordEvent(final Event event) {
    recordedEvents.add(event);
  }

  public void dispatchEvents(final Consumer<Event> eventDispatcher) {
    recordedEvents.forEach(eventDispatcher);
    recordedEvents.clear();
  }
}
