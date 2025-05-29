package com.github.booking.infrastructure.messaging.show;

import com.github.booking.application.show.command.ScheduleShow;
import com.github.booking.infrastructure.messaging.FakeMessageListener;
import com.github.seedwork.core.util.Randoms;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class FakeShowMessageListener implements FakeMessageListener {

  private final ScheduleShow scheduleShow;

  public FakeShowMessageListener(final ScheduleShow scheduleShow) {
    this.scheduleShow = Objects.requireNonNull(scheduleShow);
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
    final var command = new ScheduleShow.Command(
      showId(),
      scheduleAt(),
      movieId(),
      hallId());

    scheduleShow.handle(command);
  }
}
