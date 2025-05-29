package com.github.booking.domain.hall;

public final class Halls {

  private Halls() {
  }

  public static Hall newHall(final String hallId) {
    return new Hall(
      new HallId(hallId),
      "TestName",
      new SeatLayout(10, 15));
  }
}
