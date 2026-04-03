package com.github.scheduling.application.movie;

import com.github.scheduling.application.movie.query.ListMoviesQuery;
import com.github.scheduling.application.movie.query.MovieView;
import com.github.scheduling.domain.movie.MovieService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class MovieQueryHandlerImpl implements MovieQueryHandler {

  private final MovieService movieService;

  MovieQueryHandlerImpl(final MovieService movieService) {
    this.movieService = movieService;
  }

  @Override
  public List<MovieView> listMovies(final ListMoviesQuery query) {
    return movieService.listMovies().stream()
      .map(movie -> new MovieView(movie.movieId().value(), movie.title(), movie.runtimeMinutes()))
      .toList();
  }
}
