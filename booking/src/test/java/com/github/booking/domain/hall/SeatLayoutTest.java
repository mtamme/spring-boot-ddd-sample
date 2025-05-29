package com.github.booking.domain.hall;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeatLayoutTest {

  @Test
  void newSeatLayoutShouldReturnSeatLayout() {
    // Arrange
    // Act
    final var seatLayout = new SeatLayout(10, 15);

    // Assert
    assertEquals(10, seatLayout.rowCount());
    assertEquals(15, seatLayout.columnCount());
    assertEquals(150, seatLayout.seatCount());
  }

  @Test
  void seatNumbersShouldReturnSeatNumbers() {
    // Arrange
    final var seatLayout = new SeatLayout(10, 15);

    // Act
    final var seatNumbers = seatLayout.seatNumbers();

    // Assert
    assertEquals(150, seatNumbers.size());
    assertEquals(new SeatNumber("A1"), seatNumbers.getFirst());
    assertEquals(new SeatNumber("J15"), seatNumbers.getLast());
  }
}
