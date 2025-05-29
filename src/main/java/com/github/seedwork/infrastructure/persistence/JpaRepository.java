package com.github.seedwork.infrastructure.persistence;

public interface JpaRepository<T> {

  void persist(T entity);

  void remove(T entity);
}
