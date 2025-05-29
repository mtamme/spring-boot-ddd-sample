package com.github.seedwork.domain;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;
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
    final var problem = Problem.of("invariant-violated", "Invariant violated");

    // Act
    // Assert
    assertDoesNotThrow(() -> Contract.check(true, problem));
  }

  @Test
  void checkWithFalseConditionAndExceptionSupplierShouldThrowSuppliedException() {
    // Arrange
    final var problem = Problem.of("invariant-violated", "Invariant violated");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> Contract.check(false, problem));

    assertEquals(problem, exception.getProblem());
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
    final var problem = Problem.of("precondition-violated", "Precondition violated");

    // Act
    // Assert
    assertDoesNotThrow(() -> Contract.require(true, problem));
  }

  @Test
  void requireWithFalseConditionAndExceptionSupplierShouldThrowSuppliedException() {
    // Arrange
    final var problem = Problem.of("precondition-violated", "Precondition violated");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> Contract.require(false, problem));

    assertEquals(problem, exception.getProblem());
  }
}
