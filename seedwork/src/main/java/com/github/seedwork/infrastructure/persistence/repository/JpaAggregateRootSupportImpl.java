package com.github.seedwork.infrastructure.persistence.repository;

import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.infrastructure.event.EventPublisher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.Objects;

public class JpaAggregateRootSupportImpl implements JpaAggregateRootSupport {

  private final EntityManager entityManager;
  private final EventPublisher eventPublisher;

  public JpaAggregateRootSupportImpl(final EntityManager entityManager, final EventPublisher eventPublisher) {
    this.entityManager = Objects.requireNonNull(entityManager);
    this.eventPublisher = Objects.requireNonNull(eventPublisher);
  }

  private void save(final AggregateRoot aggregateRoot) {
    if (entityManager.contains(aggregateRoot)) {
      entityManager.lock(aggregateRoot, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    } else {
      entityManager.persist(aggregateRoot);
    }
  }

  private void publishEvents(final String aggregateId, final AggregateRoot aggregateRoot) {
    final var groupId = "%s:%s".formatted(aggregateRoot.getClass().getSimpleName(), aggregateId);

    aggregateRoot.dispatchEvents(e -> eventPublisher.publishEvent(groupId, e));
  }

  @Override
  public void saveAndPublishEvents(final String aggregateId, final AggregateRoot aggregateRoot) {
    save(aggregateRoot);
    publishEvents(aggregateId, aggregateRoot);
  }
}
