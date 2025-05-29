package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.GetShow;
import com.github.booking.domain.show.ShowFixture;
import com.github.booking.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaGetShowTest extends PersistenceTest {

  @Autowired
  private JpaShowRepository showRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void handleShouldReturnShow() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });
    final var handler = new JpaGetShow(showRepository);
    final var query = new GetShow.Query("S00000000000000000");

    // Act
    final var show = handler.handle(query);

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
}
