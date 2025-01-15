package com.github.task.management.domain.project;

import com.github.seedwork.domain.ValueObject;

import java.util.Objects;

public record ProjectId(String value) implements ValueObject {

  public ProjectId(final String value) {
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public String toString() {
    return value();
  }
}
