package com.github.booking.infrastructure.messaging;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Profile("!test")
public class MockMessageListenerContainer implements ApplicationRunner {

  private final List<MockMessageListener> listeners;

  public MockMessageListenerContainer(final List<MockMessageListener> listeners) {
    this.listeners = Objects.requireNonNull(listeners);
  }

  @Override
  public void run(final ApplicationArguments arguments) {
    listeners.forEach(MockMessageListener::onMessage);
  }
}
