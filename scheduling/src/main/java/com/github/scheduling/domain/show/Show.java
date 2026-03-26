package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.Hall;
import com.github.scheduling.domain.movie.Movie;
import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.domain.Contract;

import java.time.Instant;
import java.util.Objects;

public class Show extends AggregateRoot {

  private ShowId showId;
  private Instant scheduledAt;
  private Movie movie;
  private Hall hall;

  public Show(final ShowId showId,
              final Instant scheduledAt,
              final Movie movie,
              final Hall hall,
              final Instant now) {
    Contract.require(showId != null);
    Contract.require(scheduledAt != null);
    Contract.require(movie != null);
    Contract.require(hall != null);
    Contract.require(now != null);
    Contract.check(scheduledAt.isAfter(now), ShowException::pastSchedule);

    this.showId = showId;
    this.scheduledAt = scheduledAt;
    this.movie = movie;
    this.hall = hall;

    raiseEvent(new ShowScheduled(showId, movie.movieId(), hall.hallId(), scheduledAt));
  }

  public ShowId showId() {
    return showId;
  }

  public Instant scheduledAt() {
    return scheduledAt;
  }

  public Movie movie() {
    return movie;
  }

  public Hall hall() {
    return hall;
  }

  private Long id;

  protected Show() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof final Show other)) {
      return false;
    }

    return Objects.equals(other.showId(), showId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(showId());
  }
}
