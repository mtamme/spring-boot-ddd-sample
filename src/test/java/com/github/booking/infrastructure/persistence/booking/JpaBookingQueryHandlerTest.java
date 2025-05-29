package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.application.booking.query.GetBookingQuery;
import com.github.booking.application.booking.query.ListBookingsQuery;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.show.ShowFixture;
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
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
      final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
    });
    final var bookingQueryHandler = new JpaBookingQueryHandler(bookingRepository);
    final var query = new GetBookingQuery("B00000000000000000");

    // Act
    final var booking = bookingQueryHandler.getBooking(query);

    // Assert
    assertEquals("B00000000000000000", booking.bookingId());
    assertEquals("INITIATED", booking.status());
    final var show = booking.show();

    assertEquals("S00000000000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
  }

  @Test
  void listBookingsShouldReturnBookings() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var show = ShowFixture.newShow("S00000000000000000");

      showRepository.save(show);
      final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
    });
    final var bookingQueryHandler = new JpaBookingQueryHandler(bookingRepository);
    final var query = new ListBookingsQuery(0L, 1);

    // Act
    final var bookings = bookingQueryHandler.listBookings(query);

    // Assert
    assertEquals(1, bookings.size());
    final var booking = bookings.getFirst();

    assertEquals("B00000000000000000", booking.bookingId());
    final var show = booking.show();

    assertEquals("S00000000000000000", show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
  }
}
