package com.github.seedwork.domain;

public record TestEvent(boolean dispatchable) implements Event {

  public TestEvent() {
    this(true);
  }
}
