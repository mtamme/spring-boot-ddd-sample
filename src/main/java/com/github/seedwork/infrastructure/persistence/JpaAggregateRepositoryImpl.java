package com.github.seedwork.infrastructure.persistence;

import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.infrastructure.event.EventDispatcher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class JpaAggregateRepositoryImpl<T extends AggregateRoot> implements JpaAggregateRepository<T> {

  private final EntityManager entityManager;
  private final EventDispatcher eventDispatcher;

  public JpaAggregateRepositoryImpl(final EntityManager entityManager, final EventDispatcher eventDispatcher) {
    this.entityManager = Objects.requireNonNull(entityManager);
    this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
  }

  @Override
  public void save(final T aggregateRoot) {
    aggregateRoot.dispatchEvents(eventDispatcher::dispatchEvent);

    if (entityManager.contains(aggregateRoot)) {
      entityManager.lock(aggregateRoot, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    } else {
      entityManager.persist(aggregateRoot);
    }
  }

  @Override
  public void delete(final T aggregateRoot) {
    aggregateRoot.dispatchEvents(eventDispatcher::dispatchEvent);

    entityManager.remove(aggregateRoot);
  }
}
