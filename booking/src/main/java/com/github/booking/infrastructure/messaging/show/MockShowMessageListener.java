package com.github.booking.infrastructure.messaging.show;

import com.github.booking.application.show.ShowCommandHandler;
import com.github.booking.application.show.command.ScheduleShowCommand;
import com.github.booking.infrastructure.messaging.MockMessageListener;
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

  private String showId() {
    return "S0%016X".formatted(RandomSupport.nextLong());
  }

  private Instant scheduleAt() {
    return Instant.now()
      .truncatedTo(ChronoUnit.HOURS)
      .plus(7L, ChronoUnit.DAYS);
  }

  private String movieId() {
    return "M0%016X".formatted(RandomSupport.nextLong());
  }

  private String hallId() {
    return "H0%016X".formatted(RandomSupport.nextLong());
  }

  @Override
  public void onMessage() {
    final var command = new ScheduleShowCommand(
      showId(),
      scheduleAt(),
      movieId(),
      hallId());

    showCommandHandler.scheduleShow(command);
  }
}
