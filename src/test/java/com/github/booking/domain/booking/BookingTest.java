package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.Shows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

  @Test
  void newBookingShouldReturnInitiatedBookingAndRaiseBookingInitiatedEvent() {
    // Arrange
    // Act
    final var booking = new Booking(Shows.newShow("40000000000"), new BookingId("10000000000"));

    // Assert
    assertEquals(1, booking.events().size());
    assertInstanceOf(BookingInitiated.class, booking.events().getFirst());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(BookingStatus.INITIATED, booking.status());
  }

  @Test
  void confirmWithInitiatedBookingShouldReturnConfirmedBookingAndRaiseBookingConfirmedEvent() {
    // Arrange
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

    // Act
    booking.confirm();

    // Assert
    assertEquals(1, booking.events().size());
    assertInstanceOf(BookingConfirmed.class, booking.events().getFirst());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmWithConfirmedBookingShouldReturnConfirmedBookingAndRaiseNoEvent() {
    // Arrange
    final var booking = Bookings.newConfirmedBooking("40000000000", "10000000000");

    // Act
    booking.confirm();

    // Assert
    assertEquals(0, booking.events().size());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmWithCancelledBookingShouldThrowBookingNotConfirmableException() {
    // Arrange
    final var booking = Bookings.newCancelledBooking("40000000000", "10000000000");

    // Act
    // Assert
    assertThrows(BookingNotConfirmableException.class, booking::confirm);
  }

  @Test
  void cancelWithInitiatedBookingShouldReturnCancelledBookingAndRaiseBookingCancelledEvent() {
    // Arrange
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

    // Act
    booking.cancel();

    // Assert
    assertEquals(1, booking.events().size());
    assertInstanceOf(BookingCancelled.class, booking.events().getFirst());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }

  @Test
  void cancelWithConfirmedBookingShouldThrowBookingNotCancelableException() {
    // Arrange
    final var booking = Bookings.newConfirmedBooking("40000000000", "10000000000");

    // Act
    // Assert
    assertThrows(BookingNotCancelableException.class, booking::cancel);
  }

  @Test
  void cancelWithCancelledBookingShouldReturnCancelledBookingAndRaiseNoEvent() {
    // Arrange
    final var booking = Bookings.newCancelledBooking("40000000000", "10000000000");

    // Act
    booking.cancel();

    // Assert
    assertEquals(0, booking.events().size());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }
}
