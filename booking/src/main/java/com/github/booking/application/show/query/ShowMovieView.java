package com.github.booking.application.show.query;

public record ShowMovieView(String movieId, String title) {

  public static ShowMovieView of(final String movieId, final String title) {
    if (movieId == null) {
      return null;
    }

    return new ShowMovieView(movieId, title);
  }
}
