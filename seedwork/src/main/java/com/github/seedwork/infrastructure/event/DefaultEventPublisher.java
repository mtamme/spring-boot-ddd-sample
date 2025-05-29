package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Objects;

public class DefaultEventPublisher implements EventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventPublisher.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  public DefaultEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
  }

  @Override
  public void publishEvent(final String groupId, final Event event) {
    try {
      applicationEventPublisher.publishEvent(event);
      LOGGER.debug("Published event (groupId={}, event={})",
        groupId,
        event.getClass().getSimpleName());
    } catch (final Exception e) {
      LOGGER.error("Failed to publish event (groupId={}, event={})",
        groupId,
        event.getClass().getSimpleName(),
        e);

      throw e;
    }
  }
}
