package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.GetShowQuery;
import com.github.booking.application.show.query.ListSeatsQuery;
import com.github.booking.application.show.query.ListShowsQuery;
import com.github.booking.application.show.query.SearchShowsQuery;
import com.github.booking.domain.show.ShowFixture;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaShowQueryHandlerTest extends PersistenceTest {

  @Autowired
  private JpaShowRepository showRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void getShowShouldReturnShow() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });
    final var showQueryHandler = new JpaShowQueryHandler(showRepository);
    final var query = new GetShowQuery("S00000000000000000");

    // Act
    final var show = showQueryHandler.getShow(query);

    // Assert
    assertEquals("S00000000000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    final var movie = show.movie();

    assertEquals("M00000000000000000", movie.movieId());
    assertEquals("TestTitle", movie.title());
    final var hall = show.hall();

    assertEquals("H00000000000000000", hall.hallId());
    assertEquals("TestName", hall.name());
  }

  @Test
  void listShowsShouldReturnShows() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });
    final var showQueryHandler = new JpaShowQueryHandler(showRepository);
    final var query = new ListShowsQuery(0L, 1);

    // Act
    final var shows = showQueryHandler.listShows(query);

    // Assert
    assertEquals(1, shows.size());
    final var show = shows.getFirst();

    assertEquals("S00000000000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    final var movie = show.movie();

    assertEquals("M00000000000000000", movie.movieId());
    assertEquals("TestTitle", movie.title());
    final var hall = show.hall();

    assertEquals("H00000000000000000", hall.hallId());
    assertEquals("TestName", hall.name());
  }

  @Test
  void searchShowsShouldReturnShows() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });
    final var showQueryHandler = new JpaShowQueryHandler(showRepository);
    final var query = new SearchShowsQuery("test title", 0L, 1);

    // Act
    final var shows = showQueryHandler.searchShows(query);

    // Assert
    assertEquals(1, shows.size());
    final var show = shows.getFirst();

    assertEquals("S00000000000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    final var movie = show.movie();

    assertEquals("M00000000000000000", movie.movieId());
    assertEquals("TestTitle", movie.title());
    final var hall = show.hall();

    assertEquals("H00000000000000000", hall.hallId());
    assertEquals("TestName", hall.name());
  }

  @Test
  void listSeatsShouldReturnSeats() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });
    final var showQueryHandler = new JpaShowQueryHandler(showRepository);
    final var query = new ListSeatsQuery("S00000000000000000");

    // Act
    final var seats = showQueryHandler.listSeats(query);

    // Assert
    assertEquals(150, seats.size());
  }
}
