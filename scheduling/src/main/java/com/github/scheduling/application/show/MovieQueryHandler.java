package com.github.scheduling.application.show;

import com.github.scheduling.application.show.query.ListMoviesQuery;
import com.github.scheduling.application.show.query.MovieView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface MovieQueryHandler {
  List<MovieView> listMovies(ListMoviesQuery query);
}
