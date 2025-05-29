package com.github.booking.infrastructure.persistence.show;

import com.github.booking.domain.hall.HallId;
import com.github.booking.domain.movie.MovieId;
import com.github.booking.domain.show.ShowFixture;
import com.github.booking.domain.show.ShowId;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaShowRepositoryTest extends PersistenceTest {

  @Autowired
  private JpaShowRepository showRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void findByShowIdShouldReturnShow() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = showRepository.findByShowId(new ShowId("S00000000000000000"));

      assertTrue(show.isPresent());
      assertEquals(new ShowId("S00000000000000000"), show.get().showId());
      assertEquals(Instant.EPOCH, show.get().scheduledAt());
      final var movie = show.get().movie();

      assertEquals(new MovieId("M00000000000000000"), movie.movieId());
      assertEquals("TestTitle", movie.title());
      final var hall = show.get().hall();

      assertEquals(new HallId("H00000000000000000"), hall.hallId());
      assertEquals("TestName", hall.name());
      final var seats = show.get().seats();

      assertEquals(150, seats.size());
    });
  }

  @Test
  void saveShouldSaveShow() {
    // Arrange
    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });

    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = showRepository.findByShowId(new ShowId("S00000000000000000"));

      assertTrue(show.isPresent());
      assertEquals(new ShowId("S00000000000000000"), show.get().showId());
      assertEquals(Instant.EPOCH, show.get().scheduledAt());
      final var movie = show.get().movie();

      assertEquals(new MovieId("M00000000000000000"), movie.movieId());
      assertEquals("TestTitle", movie.title());
      final var hall = show.get().hall();

      assertEquals(new HallId("H00000000000000000"), hall.hallId());
      assertEquals("TestName", hall.name());
      final var seats = show.get().seats();

      assertEquals(150, seats.size());
    });
  }

  @Test
  void saveWithDuplicateShowIdShouldThrowDataIntegrityViolationException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });

    // Act
    // Assert
    assertThrows(DataIntegrityViolationException.class, () -> transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    }));
  }
}
