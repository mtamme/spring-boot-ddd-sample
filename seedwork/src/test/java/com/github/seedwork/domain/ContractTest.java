package com.github.seedwork.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContractTest {

  @Test
  void checkWithTrueConditionShouldNotThrow() {
    // Arrange
    // Act
    // Assert
    assertDoesNotThrow(() -> Contract.check(true));
  }

  @Test
  void checkWithFalseConditionShouldThrowIllegalStateException() {
    // Arrange
    // Act
    // Assert
    final var exception = assertThrows(IllegalStateException.class, () -> Contract.check(false));

    assertEquals("Invariant violated", exception.getMessage());
  }

  @Test
  void checkWithTrueConditionAndExceptionSupplierShouldNotThrow() {
    // Arrange
    // Act
    // Assert
    assertDoesNotThrow(() -> Contract.check(true, TestException::invariant));
  }

  @Test
  void checkWithFalseConditionAndExceptionSupplierShouldThrowSuppliedException() {
    // Arrange
    // Act
    // Assert
    final var exception = assertThrows(TestException.class, () -> Contract.check(false, TestException::invariant));

    assertEquals(TestException.INVARIANT_PROBLEM, exception.getProblem());
  }

  @Test
  void requireWithTrueConditionShouldNotThrow() {
    // Arrange
    // Act
    // Assert
    assertDoesNotThrow(() -> Contract.require(true));
  }

  @Test
  void requireWithFalseConditionShouldThrowIllegalArgumentException() {
    // Arrange
    // Act
    // Assert
    final var exception = assertThrows(IllegalArgumentException.class, () -> Contract.require(false));

    assertEquals("Precondition violated", exception.getMessage());
  }

  @Test
  void requireWithTrueConditionAndExceptionSupplierShouldNotThrow() {
    // Arrange
    // Act
    // Assert
    assertDoesNotThrow(() -> Contract.require(true, TestException::precondition));
  }

  @Test
  void requireWithFalseConditionAndExceptionSupplierShouldThrowSuppliedException() {
    // Arrange
    // Act
    // Assert
    final var exception = assertThrows(TestException.class, () -> Contract.require(false, TestException::precondition));

    assertEquals(TestException.PRECONDITION_PROBLEM, exception.getProblem());
  }
}
