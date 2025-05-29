package com.github.seedwork.infrastructure.outbox;

public record MessageCounts(int activeCount,
                            int activeLockedCount,
                            int failedCount,
                            int failedLockedCount) {

  public boolean hasFailed() {
    return (failedCount() > 0) || (failedLockedCount() > 0);
  }

  public int totalCount() {
    return activeCount() + activeLockedCount() + failedCount() + failedLockedCount();
  }
}
