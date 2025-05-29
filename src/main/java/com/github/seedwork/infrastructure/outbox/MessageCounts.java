package com.github.seedwork.infrastructure.outbox;

public record MessageCounts(int deliverableCount,
                            int deliverableLockedCount,
                            int undeliverableCount,
                            int undeliverableLockedCount) {

  public boolean hasUndeliverable() {
    return (undeliverableCount() > 0) || (undeliverableLockedCount() > 0);
  }

  public int totalCount() {
    return deliverableCount() + deliverableLockedCount() + undeliverableCount() + undeliverableLockedCount();
  }
}
