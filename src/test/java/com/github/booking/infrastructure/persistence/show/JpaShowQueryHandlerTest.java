package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.ShowQueryHandler;
import com.github.booking.domain.show.ShowRepository;
import com.github.booking.domain.show.Shows;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaShowQueryHandlerTest extends PersistenceTest {

  @Autowired
  private ShowRepository showRepository;
  @Autowired
  private ShowQueryHandler showQueryHandler;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void getShowShouldReturnShow() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("S0000000000");

      showRepository.save(show);
    });

    // Act
    final var show = showQueryHandler.getShow("S0000000000");

    // Assert
    assertEquals("S0000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    final var movie = show.movie();

    assertEquals("M0000000000", movie.movieId());
    assertEquals("TestTitle", movie.title());
    final var hall = show.hall();

    assertEquals("H0000000000", hall.hallId());
    assertEquals("TestName", hall.name());
  }

  @Test
  void listShowsShouldReturnShows() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("S0000000000");

      showRepository.save(show);
    });

    // Act
    final var shows = showQueryHandler.listShows(0L, 1);

    // Assert
    assertEquals(1, shows.size());
    final var show = shows.getFirst();

    assertEquals("S0000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    final var movie = show.movie();

    assertEquals("M0000000000", movie.movieId());
    assertEquals("TestTitle", movie.title());
    final var hall = show.hall();

    assertEquals("H0000000000", hall.hallId());
    assertEquals("TestName", hall.name());
  }

  @Test
  void searchShowsShouldReturnShows() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("S0000000000");

      showRepository.save(show);
    });

    // Act
    final var shows = showQueryHandler.searchShows("test title", 0L, 1);

    // Assert
    assertEquals(1, shows.size());
    final var show = shows.getFirst();

    assertEquals("S0000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    final var movie = show.movie();

    assertEquals("M0000000000", movie.movieId());
    assertEquals("TestTitle", movie.title());
    final var hall = show.hall();

    assertEquals("H0000000000", hall.hallId());
    assertEquals("TestName", hall.name());
  }

  @Test
  void listSeatsShouldReturnSeats() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("S0000000000");

      showRepository.save(show);
    });

    // Act
    final var seats = showQueryHandler.listSeats("S0000000000");

    // Assert
    assertEquals(150, seats.size());
  }
}
