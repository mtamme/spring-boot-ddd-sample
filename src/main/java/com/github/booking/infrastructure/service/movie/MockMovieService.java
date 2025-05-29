package com.github.booking.infrastructure.service.movie;

import com.github.booking.domain.movie.Movie;
import com.github.booking.domain.movie.MovieId;
import com.github.booking.domain.movie.MovieService;
import org.springframework.stereotype.Service;

@Service
public class MockMovieService implements MovieService {

  @Override
  public Movie movieFrom(final MovieId movieId) {
    final var title = "Movie %d".formatted(1 + Math.abs(movieId.hashCode()) % 10);

    return new Movie(movieId, title);
  }
}
