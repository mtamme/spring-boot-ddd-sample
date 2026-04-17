package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.HallId;
import com.github.scheduling.domain.movie.MovieId;
import com.github.seedwork.domain.Contract;

import java.time.Instant;

public class ShowScheduled extends ShowEvent {

  private final MovieId movieId;
  private final HallId hallId;
  private final Instant scheduledAt;

  public ShowScheduled(final ShowId showId,
                       final MovieId movieId,
                       final HallId hallId,
                       final Instant scheduledAt) {
    super(showId);
    Contract.require(movieId != null);
    Contract.require(hallId != null);
    Contract.require(scheduledAt != null);

    this.movieId = movieId;
    this.hallId = hallId;
    this.scheduledAt = scheduledAt;
  }

  public MovieId movieId() {
    return movieId;
  }

  public HallId hallId() {
    return hallId;
  }

  public Instant scheduledAt() {
    return scheduledAt;
  }
}
