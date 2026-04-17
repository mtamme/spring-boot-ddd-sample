package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.HallId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowSchedulingPolicyTest {

  @Mock
  private ShowRepository showRepository;

  @Test
  void ensureNoOverlapWithNoOverlapShouldNotThrow() {
    // Arrange
    final var hallId = new HallId("H00000000000000000");
    final var start = Instant.parse("2026-01-08T00:00:00Z");
    final var end = Instant.parse("2026-01-08T02:00:00Z");

    when(showRepository.countOverlappingShows(hallId, start, end))
      .thenReturn(0L);
    final var policy = new ShowSchedulingPolicy(showRepository);

    // Act & Assert
    assertDoesNotThrow(() -> policy.ensureNoOverlap(hallId, start, end));
  }

  @Test
  void ensureNoOverlapWithOverlapShouldThrowShowException() {
    // Arrange
    final var hallId = new HallId("H00000000000000000");
    final var start = Instant.parse("2026-01-08T00:00:00Z");
    final var end = Instant.parse("2026-01-08T02:00:00Z");

    when(showRepository.countOverlappingShows(hallId, start, end))
      .thenReturn(1L);
    final var policy = new ShowSchedulingPolicy(showRepository);

    // Act & Assert
    assertThrows(ShowException.class, () -> policy.ensureNoOverlap(hallId, start, end));
  }
}
