package com.github.scheduling.domain.hall;

public final class HallFixture {

  private HallFixture() {
  }

  public static Hall newHall(final String hallId) {
    return new Hall(new HallId(hallId), "TestName", 150);
  }
}
