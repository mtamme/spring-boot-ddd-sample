package com.github.scheduling.domain.show;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.Event;

public abstract class ShowEvent implements Event {

  private final ShowId showId;

  protected ShowEvent(final ShowId showId) {
    Contract.require(showId != null);

    this.showId = showId;
  }

  public ShowId showId() {
    return showId;
  }
}
