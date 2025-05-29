package com.github.booking.domain.movie;

public final class Movies {

  private Movies() {
  }

  public static Movie newMovie(final String movieId) {
    return new Movie(new MovieId(movieId), "TestTitle");
  }
}
