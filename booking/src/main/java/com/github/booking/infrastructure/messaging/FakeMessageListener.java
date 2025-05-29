package com.github.booking.infrastructure.messaging;

@FunctionalInterface
public interface FakeMessageListener {

  void onMessage();
}
