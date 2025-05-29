package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import jakarta.persistence.EntityManager;

import java.util.Objects;

public class JpaMessageEnqueuerImpl implements JpaMessageEnqueuer {

  private final EntityManager entityManager;

  public JpaMessageEnqueuerImpl(final EntityManager entityManager) {
    this.entityManager = Objects.requireNonNull(entityManager);
  }

  @Override
  public void enqueue(final Message message) {
    entityManager.persist(message);
  }
}
