package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.query.GetBooking;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.show.ShowFixture;
import com.github.booking.infrastructure.persistence.PersistenceTest;
import com.github.booking.infrastructure.persistence.show.JpaShowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaGetBookingTest extends PersistenceTest {

  @Autowired
  private JpaBookingRepository bookingRepository;
  @Autowired
  private JpaShowRepository showRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void handleShouldReturnBooking() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
      final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
    });
    final var handler = new JpaGetBooking(bookingRepository);
    final var query = new GetBooking.Query("B00000000000000000");

    // Act
    final var booking = handler.handle(query);

    // Assert
    assertEquals("B00000000000000000", booking.bookingId());
    assertEquals("INITIATED", booking.status());
    final var show = booking.show();

    assertEquals("S00000000000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
  }
}
