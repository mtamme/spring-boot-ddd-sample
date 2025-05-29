package com.github.booking.domain.movie;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

public record Movie(MovieId movieId, String title) implements ValueObject {

  public Movie {
    Contract.require(movieId != null);
    Contract.require((title != null) && !title.isBlank());
  }
}
