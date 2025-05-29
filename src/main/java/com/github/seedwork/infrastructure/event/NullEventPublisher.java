package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.Event;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Component
@Fallback
public class NullEventPublisher implements EventPublisher {

  @Override
  public void publishEvent(final String groupId, final Event event) {
    // Do nothing
  }
}
