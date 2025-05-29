package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.Event;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Objects;

public class DefaultEventPublisher implements EventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public DefaultEventPublisher(final ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = Objects.requireNonNull(eventPublisher);
  }

  @Override
  public void publishEvent(final String groupId, final Event event) {
    eventPublisher.publishEvent(event);
  }
}
