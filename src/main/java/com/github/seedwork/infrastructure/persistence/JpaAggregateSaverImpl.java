package com.github.seedwork.infrastructure.persistence;

import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.infrastructure.event.EventPublisher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.Objects;

public class JpaAggregateSaverImpl<T extends AggregateRoot> implements JpaAggregateSaver<T> {

  private final EntityManager entityManager;
  private final EventPublisher eventPublisher;

  public JpaAggregateSaverImpl(final EntityManager entityManager, final EventPublisher eventPublisher) {
    this.entityManager = Objects.requireNonNull(entityManager);
    this.eventPublisher = Objects.requireNonNull(eventPublisher);
  }

  private void save(final T aggregateRoot) {
    if (entityManager.contains(aggregateRoot)) {
      entityManager.lock(aggregateRoot, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    } else {
      entityManager.persist(aggregateRoot);
    }
  }

  private void publishEvents(final String aggregateId, final T aggregateRoot) {
    final var groupId = "%s-%s".formatted(aggregateRoot.getClass().getSimpleName(), aggregateId);

    aggregateRoot.releaseEvents(e -> eventPublisher.publishEvent(groupId, e));
  }

  @Override
  public void saveAndPublishEvents(final String aggregateId, final T aggregateRoot) {
    save(aggregateRoot);
    publishEvents(aggregateId, aggregateRoot);
  }
}
