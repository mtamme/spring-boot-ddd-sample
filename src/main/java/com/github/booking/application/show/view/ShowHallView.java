package com.github.booking.application.show.view;

public record ShowHallView(String hallId, String name) {

  public static ShowHallView of(final String hallId, final String name) {
    if (hallId == null) {
      return null;
    }

    return new ShowHallView(hallId, name);
  }
}
