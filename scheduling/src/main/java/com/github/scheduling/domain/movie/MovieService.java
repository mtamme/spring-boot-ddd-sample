package com.github.scheduling.domain.movie;

import java.util.List;

public interface MovieService {

  Movie movieFrom(MovieId movieId);

  List<Movie> listMovies();
}
