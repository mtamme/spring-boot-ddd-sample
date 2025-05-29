package com.github.booking.infrastructure.messaging.show;

import com.github.booking.application.show.ShowCommandHandler;
import com.github.booking.application.show.command.ScheduleShowCommand;
import com.github.booking.infrastructure.messaging.FakeMessageListener;
import com.github.seedwork.core.util.Randoms;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class FakeShowMessageListener implements FakeMessageListener {

  private final ShowCommandHandler showCommandHandler;

  public FakeShowMessageListener(final ShowCommandHandler showCommandHandler) {
    this.showCommandHandler = Objects.requireNonNull(showCommandHandler);
  }

  private String showId() {
    return "S0%016X".formatted(Randoms.nextLong());
  }

  private Instant scheduleAt() {
    return Instant.now()
      .truncatedTo(ChronoUnit.HOURS)
      .plus(7L, ChronoUnit.DAYS);
  }

  private String movieId() {
    return "M0%016X".formatted(Randoms.nextLong());
  }

  private String hallId() {
    return "H0%016X".formatted(Randoms.nextLong());
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
