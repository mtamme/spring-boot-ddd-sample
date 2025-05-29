package com.github.booking.infrastructure.messaging;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Profile("!test")
public class FakeMessageListenerContainer implements ApplicationRunner {

  private final List<FakeMessageListener> listeners;

  public FakeMessageListenerContainer(final List<FakeMessageListener> listeners) {
    this.listeners = Objects.requireNonNull(listeners);
  }

  @Override
  public void run(@NonNull final ApplicationArguments arguments) {
    listeners.forEach(FakeMessageListener::onMessage);
  }
}
