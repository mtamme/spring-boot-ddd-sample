package com.github.scheduling.infrastructure.service.movie;

import com.github.scheduling.domain.movie.Movie;
import com.github.scheduling.domain.movie.MovieId;
import com.github.scheduling.domain.movie.MovieService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FakeMovieService implements MovieService {

  private static final List<Movie> MOVIES = List.of(
    new Movie(new MovieId("M00000000000000001"), "The Grand Adventure", 120),
    new Movie(new MovieId("M00000000000000002"), "Silent Echoes", 95),
    new Movie(new MovieId("M00000000000000003"), "Starbound", 142),
    new Movie(new MovieId("M00000000000000004"), "The Last Horizon", 108),
    new Movie(new MovieId("M00000000000000005"), "City of Shadows", 130));

  @Override
  public Movie getMovie(final MovieId movieId) {
    return MOVIES.stream()
      .filter(movie -> movie.movieId().equals(movieId))
      .findFirst()
      .orElseGet(() -> {
        final var title = "Movie %d".formatted(1 + Math.abs(movieId.hashCode()) % 10);
        final var runtimeMinutes = 90 + Math.abs(movieId.hashCode()) % 91;

        return new Movie(movieId, title, runtimeMinutes);
      });
  }

  @Override
  public List<Movie> listMovies() {
    return MOVIES;
  }
}
