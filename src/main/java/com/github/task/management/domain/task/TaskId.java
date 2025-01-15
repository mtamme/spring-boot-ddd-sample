package com.github.task.management.domain.task;

import com.github.seedwork.domain.ValueObject;

import java.util.Objects;

public record TaskId(String value) implements ValueObject {

  public TaskId(final String value) {
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public String toString() {
    return value();
  }
}
