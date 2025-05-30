package com.github.seedwork.infrastructure.persistence;

import com.github.seedwork.domain.AggregateRoot;

public interface JpaAggregateRepository<T extends AggregateRoot> {

  void save(T aggregateRoot);

  void delete(T aggregateRoot);
}
