package com.github.scheduling.domain.movie;

import java.util.List;

public interface MovieService {

  Movie getMovie(MovieId movieId);

  List<Movie> listMovies();
}
