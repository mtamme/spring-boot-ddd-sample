package com.github.seedwork.application;

@FunctionalInterface
public interface CommandHandler<C> {

  void handle(C command);
}
