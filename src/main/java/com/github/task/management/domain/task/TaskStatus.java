package com.github.task.management.domain.task;

import com.github.seedwork.domain.ValueObject;

public enum TaskStatus implements ValueObject {

  OPEN {
    @Override
    public boolean isOpen() {
      return true;
    }
  },
  STARTED {
    @Override
    public boolean isStarted() {
      return true;
    }
  },
  CLOSED {
    @Override
    public boolean isClosed() {
      return true;
    }
  };

  public boolean isOpen() {
    return false;
  }

  public boolean isStarted() {
    return false;
  }

  public boolean isClosed() {
    return false;
  }
}
