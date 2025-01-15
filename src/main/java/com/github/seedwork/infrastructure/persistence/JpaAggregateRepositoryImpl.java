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
  public void save(final T entity) {
    entity.dispatchEvents(eventDispatcher::dispatchEvent);

    if (entityManager.contains(entity)) {
      entityManager.lock(entity, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    } else {
      entityManager.persist(entity);
    }
  }

  @Override
  public void delete(final T entity) {
    entity.dispatchEvents(eventDispatcher::dispatchEvent);

    entityManager.remove(entity);
  }
}
