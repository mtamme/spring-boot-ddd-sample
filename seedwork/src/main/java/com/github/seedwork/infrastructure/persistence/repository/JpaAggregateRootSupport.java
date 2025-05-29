package com.github.seedwork.infrastructure.persistence.repository;

import com.github.seedwork.domain.AggregateRoot;

public interface JpaAggregateRootSupport {

  void saveAndPublishEvents(String aggregateId, AggregateRoot aggregateRoot);
}
