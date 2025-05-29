package com.github.booking.infrastructure.messaging.show;

import com.github.booking.application.show.ShowCommandHandler;
import com.github.booking.application.show.command.ScheduleShowCommand;
import com.github.booking.infrastructure.messaging.MockMessageListener;
import com.github.seedwork.core.util.Base64Support;
import com.github.seedwork.core.util.RandomSupport;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class MockShowMessageListener implements MockMessageListener {

  private final ShowCommandHandler showCommandHandler;

  public MockShowMessageListener(final ShowCommandHandler showCommandHandler) {
    this.showCommandHandler = Objects.requireNonNull(showCommandHandler);
  }

  @Override
  public void onMessage() {
    final var command = new ScheduleShowCommand(
      Base64Support.encodeLong(RandomSupport.nextLong()),
      Instant.now()
        .truncatedTo(ChronoUnit.HOURS)
        .plus(7L, ChronoUnit.DAYS),
      Base64Support.encodeLong(RandomSupport.nextLong()),
      Base64Support.encodeLong(RandomSupport.nextLong()));

    showCommandHandler.scheduleShow(command);
  }
}
