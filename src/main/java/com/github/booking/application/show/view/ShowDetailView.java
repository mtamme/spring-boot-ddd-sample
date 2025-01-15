package com.github.booking.application.show.view;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record ShowDetailView(String showId,
                             Instant scheduledAt,
                             ShowMovieView movie,
                             ShowHallView hall,
                             List<SeatView> seats) {

  public ShowDetailView(final String showId,
                        final Instant scheduledAt,
                        final String movieId,
                        final String movieTitle,
                        final String hallId,
                        final String hallName) {
    this(showId, scheduledAt, ShowMovieView.of(movieId, movieTitle), ShowHallView.of(hallId, hallName), new ArrayList<>());
  }
}
