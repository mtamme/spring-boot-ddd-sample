package com.github.seedwork.infrastructure.outbox;

public record MessageCounts(int activeCount,
                            int failedCount,
                            int lockedCount) {

  public boolean hasFailed() {
    return (failedCount() > 0);
  }

  public int totalCount() {
    return activeCount() + failedCount();
  }
}
