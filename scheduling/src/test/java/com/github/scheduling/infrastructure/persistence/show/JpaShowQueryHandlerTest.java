package com.github.scheduling.infrastructure.persistence.show;

import com.github.scheduling.application.show.query.GetShowQuery;
import com.github.scheduling.application.show.query.ListShowsQuery;
import com.github.scheduling.domain.show.ShowFixture;
import com.github.scheduling.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

class JpaShowQueryHandlerTest extends PersistenceTest {

  @Autowired
  private JpaShowRepository repository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void getShowShouldReturnShowDetailView() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");
      repository.save(show);
    });
    final var queryHandler = new JpaShowQueryHandler(repository);

    // Act
    final var result = queryHandler.getShow(new GetShowQuery("S00000000000000000"));

    // Assert
    assertEquals("S00000000000000000", result.showId());
    assertEquals("TestTitle", result.movieTitle());
    assertEquals("TestName", result.hallName());
    assertEquals(120, result.movieRuntimeMinutes());
    assertEquals(150, result.hallSeatCount());
  }

  @Test
  void listShowsShouldReturnShowSummaryViews() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      repository.save(ShowFixture.newShow("S00000000000000001"));
      repository.save(ShowFixture.newShow("S00000000000000002"));
    });
    final var queryHandler = new JpaShowQueryHandler(repository);

    // Act
    final var results = queryHandler.listShows(new ListShowsQuery(0L, 10));

    // Assert
    assertEquals(2, results.size());
  }
}
