package com.github.booking.domain.movie;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class MovieException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("movie-not-found", "Movie not found");

  private MovieException(final Problem problem) {
    super(problem);
  }

  public static MovieException notFound() {
    return new MovieException(NOT_FOUND_PROBLEM);
  }
}
