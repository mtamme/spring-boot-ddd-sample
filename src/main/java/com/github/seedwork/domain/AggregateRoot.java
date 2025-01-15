package com.github.seedwork.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AggregateRoot implements Entity {

  private Integer version;
  private final List<Event> events;

  protected AggregateRoot() {
    this.events = new ArrayList<>();
  }

  public Integer version() {
    return version;
  }

  public List<Event> events() {
    return List.copyOf(events);
  }

  protected void raiseEvent(final Event event) {
    events.add(event);
  }

  public void dispatchEvents(final Consumer<Event> eventDispatcher) {
    events.forEach(eventDispatcher);
    events.clear();
  }
}
