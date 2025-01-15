package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.Event;
import org.springframework.stereotype.Component;

@Component
public class NullEventDispatcher implements EventDispatcher {

  @Override
  public void dispatchEvent(final Event event) {
    // Do nothing
  }
}
