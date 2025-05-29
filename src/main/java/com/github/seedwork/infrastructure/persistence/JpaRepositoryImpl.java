package com.github.seedwork.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class JpaRepositoryImpl<T> implements JpaRepository<T> {

  private final EntityManager entityManager;

  public JpaRepositoryImpl(final EntityManager entityManager) {
    this.entityManager = Objects.requireNonNull(entityManager);
  }

  @Override
  public void persist(final T entity) {
    entityManager.persist(entity);
  }

  @Override
  public void remove(final T entity) {
    entityManager.remove(entity);
  }
}
