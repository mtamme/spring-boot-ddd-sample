package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.Event;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Component
@Fallback
public class NullEventDispatcher implements EventDispatcher {

  @Override
  public void dispatchEvent(final String groupId, final Event event) {
    // Do nothing
  }
}
