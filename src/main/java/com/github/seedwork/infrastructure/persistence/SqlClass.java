package com.github.seedwork.infrastructure.persistence;

import java.sql.SQLException;

public enum SqlClass {

  SUCCESSFUL_COMPLETION("00"),
  INTEGRITY_CONSTRAINT_VIOLATION("23");

  private final String value;

  SqlClass(final String value) {
    this.value = value;
  }

  public boolean isSubclass(final Throwable throwable) {
    return (throwable instanceof SQLException exception) && isSubclass(exception);
  }

  public boolean isSubclass(final SQLException exception) {
    return isSubclass(exception.getSQLState());
  }

  public boolean isSubclass(final String state) {
    return (state != null) && state.startsWith(value);
  }
}
