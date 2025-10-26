package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.domain.booking.Bookings;
import com.github.booking.domain.show.Shows;
import com.github.booking.infrastructure.persistence.show.JpaShowRepository;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaBookingQueryHandlerTest extends PersistenceTest {

  @Autowired
  private JpaBookingRepository bookingRepository;
  @Autowired
  private JpaShowRepository showRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void getBookingShouldReturnBooking() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("S0000000000");

      showRepository.save(show);
      final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

      bookingRepository.save(booking);
    });
    final var bookingQueryHandler = new JpaBookingQueryHandler(bookingRepository);

    // Act
    final var booking = bookingQueryHandler.getBooking("B0000000000");

    // Assert
    assertEquals("B0000000000", booking.bookingId());
    assertEquals("INITIATED", booking.status());
    final var show = booking.show();

    assertEquals("S0000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
  }

  @Test
  void listBookingsShouldReturnBookings() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("S0000000000");

      showRepository.save(show);
      final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

      bookingRepository.save(booking);
    });
    final var bookingQueryHandler = new JpaBookingQueryHandler(bookingRepository);

    // Act
    final var bookings = bookingQueryHandler.listBookings(0L, 1);

    // Assert
    assertEquals(1, bookings.size());
    final var booking = bookings.getFirst();

    assertEquals("B0000000000", booking.bookingId());
    final var show = booking.show();

    assertEquals("S0000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
  }
}
