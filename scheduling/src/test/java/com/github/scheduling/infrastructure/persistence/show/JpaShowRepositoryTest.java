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
}
