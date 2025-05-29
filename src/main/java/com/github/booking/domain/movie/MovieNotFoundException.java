package com.github.booking.domain.movie;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class MovieNotFoundException extends ProblemException {

  private static final Problem PROBLEM = Problem.notFound("movie-not-found", "Movie not found");

  public MovieNotFoundException() {
    super(PROBLEM);
  }
}
