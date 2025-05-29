package com.github.seedwork.application;

@FunctionalInterface
public interface CommandHandlerWithResult<C, R> {

  R handle(C command);
}
