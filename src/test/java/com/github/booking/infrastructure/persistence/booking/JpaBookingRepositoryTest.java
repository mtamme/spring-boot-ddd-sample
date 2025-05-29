package com.github.booking.infrastructure.persistence.booking;

import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingStatus;
import com.github.booking.domain.show.ShowId;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

class JpaBookingRepositoryTest extends PersistenceTest {

  @Autowired
  private JpaBookingRepository bookingRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void findByBookingIdShouldReturnBooking() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
    });

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = bookingRepository.findByBookingId(new BookingId("B00000000000000000"));

      assertTrue(booking.isPresent());
      assertEquals(new ShowId("S00000000000000000"), booking.get().showId());
      assertEquals(new BookingId("B00000000000000000"), booking.get().bookingId());
      assertEquals(BookingStatus.INITIATED, booking.get().status());
    });
  }

  @Test
  void saveShouldSaveBooking() {
    // Arrange
    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
    });

    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = bookingRepository.findByBookingId(new BookingId("B00000000000000000"));

      assertTrue(booking.isPresent());
      assertEquals(new ShowId("S00000000000000000"), booking.get().showId());
      assertEquals(new BookingId("B00000000000000000"), booking.get().bookingId());
      assertEquals(BookingStatus.INITIATED, booking.get().status());
    });
  }

  @Test
  void saveWithDuplicateBookingIdShouldThrowDataIntegrityViolationException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
    });

    // Act
    // Assert
    assertThrows(DataIntegrityViolationException.class, () -> transactionTemplate.executeWithoutResult(ts -> {
      final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
    }));
  }
}
