package com.github.seedwork.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongsTest {

  @Test
  void fromBase64StringShouldReturnLongValue() {
    // Arrange
    // Act
    final var value = Longs.fromBase64String("00000000000");

    // Assert
    assertEquals(-3220860076361985203L, value);
  }

  @Test
  void toBase64StringShouldReturnBase64String() {
    // Arrange
    // Act
    final var string = Longs.toBase64String(-3220860076361985203L);

    // Assert
    assertEquals("00000000000", string);
  }
}
