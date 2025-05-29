package com.github.seedwork.domain;

public record TestEvent(boolean processable) implements Event {

  public TestEvent() {
    this(true);
  }
}
