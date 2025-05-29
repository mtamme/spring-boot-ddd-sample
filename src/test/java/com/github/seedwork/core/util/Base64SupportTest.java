package com.github.seedwork.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base64SupportTest {

  @Test
  void decodeLongShouldReturnLongValue() {
    // Arrange
    // Act
    final var value = Base64Support.decodeLong("00000000000");

    // Assert
    assertEquals(-3220860076361985203L, value);
  }

  @Test
  void encodeLongShouldReturnBase64String() {
    // Arrange
    // Act
    final var string = Base64Support.encodeLong(-3220860076361985203L);

    // Assert
    assertEquals("00000000000", string);
  }
}
