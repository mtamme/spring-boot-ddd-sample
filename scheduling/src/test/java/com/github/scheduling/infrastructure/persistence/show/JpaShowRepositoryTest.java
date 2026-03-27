package com.github.scheduling.infrastructure.persistence.show;

import com.github.scheduling.domain.hall.Hall;
import com.github.scheduling.domain.hall.HallId;
import com.github.scheduling.domain.movie.Movie;
import com.github.scheduling.domain.movie.MovieId;
import com.github.scheduling.domain.show.Show;
import com.github.scheduling.domain.show.ShowId;
import com.github.scheduling.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaShowRepositoryTest extends PersistenceTest {

  @Autowired
  private JpaShowRepository showRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void saveShouldSaveShow() {
    // Arrange
    final var showId = new ShowId("S00000000000000000");
    final var now = Instant.parse("2026-01-01T00:00:00Z");
    final var scheduledAt = Instant.parse("2026-01-08T00:00:00Z");
    final var movie = new Movie(new MovieId("M00000000000000000"), "TestMovie", 120);
    final var hall = new Hall(new HallId("H00000000000000000"), "TestHall", 150);
    final var show = new Show(showId, scheduledAt, movie, hall, now);

    // Act
    transactionTemplate.executeWithoutResult(status -> showRepository.save(show));

    // Assert
    final var found = transactionTemplate.execute(status ->
      showRepository.findByShowId(showId));

    assertTrue(found.isPresent());
    assertEquals(showId, found.get().showId());
    assertEquals(scheduledAt, found.get().scheduledAt());
    assertEquals(movie, found.get().movie());
    assertEquals(hall, found.get().hall());
  }

  @Test
  void nextShowIdShouldReturnShowId() {
    // Act
    final var showId = showRepository.nextShowId();

    // Assert
    assertNotNull(showId);
    assertTrue(showId.value().matches("S0[0-9A-F]{16}"));
  }

  @Test
  void findByShowIdWithUnknownIdShouldReturnEmpty() {
    // Act
    final var found = transactionTemplate.execute(status ->
      showRepository.findByShowId(new ShowId("S0FFFFFFFFFFFFFFFF")));

    // Assert
    assertTrue(found.isEmpty());
  }

  @Test
  void countOverlappingShowsWithOverlapShouldReturnCount() {
    // Arrange
    final var now = Instant.parse("2026-01-01T00:00:00Z");
    final var scheduledAt = Instant.parse("2026-01-08T19:00:00Z");
    final var movie = new Movie(new MovieId("M00000000000000000"), "TestMovie", 120);
    final var hall = new Hall(new HallId("H00000000000000000"), "TestHall", 150);
    final var show = new Show(new ShowId("S00000000000000000"), scheduledAt, movie, hall, now);

    transactionTemplate.executeWithoutResult(status -> showRepository.save(show));

    // Act — query a range that overlaps with [19:00, 21:00)
    final var count = transactionTemplate.execute(status ->
      showRepository.countOverlappingShows(
        new HallId("H00000000000000000"),
        Instant.parse("2026-01-08T20:00:00Z"),
        Instant.parse("2026-01-08T22:00:00Z")));

    // Assert
    assertEquals(1L, count);
  }

  @Test
  void countOverlappingShowsWithNoOverlapShouldReturnZero() {
    // Arrange
    final var now = Instant.parse("2026-01-01T00:00:00Z");
    final var scheduledAt = Instant.parse("2026-01-08T19:00:00Z");
    final var movie = new Movie(new MovieId("M00000000000000000"), "TestMovie", 120);
    final var hall = new Hall(new HallId("H00000000000000000"), "TestHall", 150);
    final var show = new Show(new ShowId("S00000000000000001"), scheduledAt, movie, hall, now);

    transactionTemplate.executeWithoutResult(status -> showRepository.save(show));

    // Act — query a range after [19:00, 21:00)
    final var count = transactionTemplate.execute(status ->
      showRepository.countOverlappingShows(
        new HallId("H00000000000000000"),
        Instant.parse("2026-01-08T21:00:00Z"),
        Instant.parse("2026-01-08T23:00:00Z")));

    // Assert
    assertEquals(0L, count);
  }
}
