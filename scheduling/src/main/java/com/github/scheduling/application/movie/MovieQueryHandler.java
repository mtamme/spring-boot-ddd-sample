package com.github.scheduling.application.movie;

import com.github.scheduling.application.movie.query.ListMoviesQuery;
import com.github.scheduling.application.movie.query.MovieView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface MovieQueryHandler {
  List<MovieView> listMovies(ListMoviesQuery query);
}
