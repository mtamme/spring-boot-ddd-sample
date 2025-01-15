package com.github.booking.domain.movie;

import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.ValueObject;

public record Movie(MovieId movieId, String title) implements ValueObject {

  public Movie {
    Contract.requireNonNull(movieId);
    Contract.requireNonNull(title);
  }
}
