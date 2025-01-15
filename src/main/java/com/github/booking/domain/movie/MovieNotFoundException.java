package com.github.booking.domain.movie;

import com.github.seedwork.core.problem.NotFoundException;

public class MovieNotFoundException extends NotFoundException {

  public MovieNotFoundException() {
    super("Movie not found");
  }
}
