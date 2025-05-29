package com.github.seedwork.infrastructure.persistence;

import com.github.seedwork.domain.AggregateRoot;

public interface JpaAggregateSaver<T extends AggregateRoot> {

  void save(String aggregateId, T aggregateRoot);
}
