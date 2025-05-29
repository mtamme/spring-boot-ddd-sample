package com.github.seedwork.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AggregateRoot implements Entity {

  private Long version;
  private final List<Event> raisedEvents;

  protected AggregateRoot() {
    raisedEvents = new ArrayList<>();
  }

  public List<Event> raisedEvents() {
    return List.copyOf(raisedEvents);
  }

  public void raiseEvent(final Event event) {
    raisedEvents.add(event);
  }

  public void releaseEvents(final Consumer<Event> eventConsumer) {
    raisedEvents.forEach(eventConsumer);
    raisedEvents.clear();
  }
}
