package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.BookingQueryHandler;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.booking.Bookings;
import com.github.booking.domain.show.ShowRepository;
import com.github.booking.domain.show.Shows;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaBookingQueryHandlerTest extends PersistenceTest {

  @Autowired
  private BookingRepository bookingRepository;
  @Autowired
  private ShowRepository showRepository;
  @Autowired
  private BookingQueryHandler bookingQueryHandler;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void getBookingShouldReturnBooking() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("40000000000");

      showRepository.save(show);
      final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

      bookingRepository.save(booking);
    });

    // Act
    final var booking = bookingQueryHandler.getBooking("10000000000");

    // Assert
    assertEquals("10000000000", booking.bookingId());
    assertEquals("INITIATED", booking.status());
    final var show = booking.show();

    assertEquals("40000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
  }

  @Test
  void listBookingsShouldReturnBookings() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = Shows.newShow("40000000000");

      showRepository.save(show);
      final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

      bookingRepository.save(booking);
    });

    // Act
    final var bookings = bookingQueryHandler.listBookings(0L, 1);

    // Assert
    assertEquals(1, bookings.size());
    final var booking = bookings.getFirst();

    assertEquals("10000000000", booking.bookingId());
    final var show = booking.show();

    assertEquals("40000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
  }
}
