package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.ListSeats;
import com.github.booking.domain.show.ShowFixture;
import com.github.booking.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

class JpaListSeatsTest extends PersistenceTest {

  @Autowired
  private JpaShowRepository showRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void handleShouldReturnSeats() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
    });
    final var handler = new JpaListSeats(showRepository);
    final var query = new ListSeats.Query("S00000000000000000");

    // Act
    final var seats = handler.handle(query);

    // Assert
    assertEquals(150, seats.size());
  }
}
