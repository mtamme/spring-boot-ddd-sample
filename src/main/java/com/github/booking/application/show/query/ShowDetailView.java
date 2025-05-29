package com.github.booking.application.show.query;

import java.time.Instant;

public record ShowDetailView(String showId,
                             Instant scheduledAt,
                             ShowMovieView movie,
                             ShowHallView hall) {

  public ShowDetailView(final String showId,
                        final Instant scheduledAt,
                        final String movieId,
                        final String movieTitle,
                        final String hallId,
                        final String hallName) {
    this(showId, scheduledAt, ShowMovieView.of(movieId, movieTitle), ShowHallView.of(hallId, hallName));
  }
}
