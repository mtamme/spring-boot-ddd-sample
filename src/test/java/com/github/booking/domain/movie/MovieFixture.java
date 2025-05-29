package com.github.booking.domain.movie;

public final class MovieFixture {

  private MovieFixture() {
  }

  public static Movie newMovie(final String movieId) {
    return new Movie(new MovieId(movieId), "TestTitle");
  }
}
