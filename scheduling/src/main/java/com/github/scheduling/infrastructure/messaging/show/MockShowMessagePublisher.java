package com.github.scheduling.infrastructure.messaging.show;

import com.github.scheduling.domain.show.ShowScheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MockShowMessagePublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockShowMessagePublisher.class);

  @EventListener
  public void onShowScheduled(final ShowScheduled event) {
    LOGGER.info("[Integration Event] ShowScheduled: showId={}, movieId={}, hallId={}, scheduledAt={}",
      event.showId().value(),
      event.movieId().value(),
      event.hallId().value(),
      event.scheduledAt());
  }
}