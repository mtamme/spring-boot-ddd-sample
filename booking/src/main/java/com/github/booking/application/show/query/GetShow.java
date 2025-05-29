package com.github.booking.application.show.query;

import com.github.seedwork.application.QueryHandler;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Transactional(readOnly = true)
public interface GetShow extends QueryHandler<GetShow.Query, GetShow.Show> {

  record Query(String showId) {
  }

  record Show(String showId,
              Instant scheduledAt,
              Movie movie,
              Hall hall) {

    public Show(final String showId,
                final Instant scheduledAt,
                final String movieId,
                final String movieTitle,
                final String hallId,
                final String hallName) {
      this(showId, scheduledAt, new Movie(movieId, movieTitle), new Hall(hallId, hallName));
    }
  }

  record Movie(String movieId, String title) {
  }

  record Hall(String hallId, String name) {
  }
}
