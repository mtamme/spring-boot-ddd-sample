package com.github.seedwork.application;

@FunctionalInterface
public interface QueryHandler<Q, R> {

  R handle(Q query);
}
