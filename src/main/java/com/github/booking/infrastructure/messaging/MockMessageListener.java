package com.github.booking.infrastructure.messaging;

@FunctionalInterface
public interface MockMessageListener {

  void onMessage();
}
