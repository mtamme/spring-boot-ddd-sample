package com.github.seedwork.application;

@FunctionalInterface
public interface EventHandler<E> {

  void handle(E event);
}
