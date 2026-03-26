package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.HallFixture;
import com.github.scheduling.domain.movie.MovieFixture;
import com.github.seedwork.core.util.Consumers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class ShowFixture {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  private ShowFixture() {
  }

  public static Show newShow(final String showId) {
    final var show = new Show(
      new ShowId(showId),
      NOW.plus(7L, ChronoUnit.DAYS),
      MovieFixture.newMovie("M00000000000000000"),
      HallFixture.newHall("H00000000000000000"),
      NOW);

    show.releaseEvents(Consumers.empty());

    return show;
  }
}
