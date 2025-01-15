package com.github.task.management.domain.collaborator;

import com.github.seedwork.domain.ValueObject;

import java.util.Objects;

public abstract class Collaborator implements ValueObject {

  private String name;

  protected Collaborator(final String name) {
    setName(name);
  }

  public String name() {
    return name;
  }

  private void setName(final String name) {
    this.name = Objects.requireNonNull(name);
  }

  protected Collaborator() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof Collaborator other)) {
      return false;
    }

    return Objects.equals(other.name(), name());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name());
  }
}
