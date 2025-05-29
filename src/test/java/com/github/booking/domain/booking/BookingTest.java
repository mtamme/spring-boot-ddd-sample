package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

  @Test
  void newBookingShouldReturnInitiatedBookingAndRaiseBookingInitiatedEvent() {
    // Arrange
    // Act
    final var booking = new Booking(new ShowId("S0000000000"), new BookingId("B0000000000"));

    // Assert
    assertEquals(1, booking.recordedEvents().size());
    assertInstanceOf(BookingInitiated.class, booking.recordedEvents().getFirst());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(BookingStatus.INITIATED, booking.status());
  }

  @Test
  void confirmWithInitiatedBookingShouldConfirmBookingAndRaiseBookingConfirmedEvent() {
    // Arrange
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    booking.confirm();

    // Assert
    assertEquals(1, booking.recordedEvents().size());
    assertInstanceOf(BookingConfirmed.class, booking.recordedEvents().getFirst());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmWithConfirmedBookingShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    booking.confirm();

    // Assert
    assertEquals(0, booking.recordedEvents().size());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmWithCancelledBookingShouldThrowBookingNotConfirmableException() {
    // Arrange
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(BookingNotConfirmableException.class, booking::confirm);
  }

  @Test
  void cancelWithInitiatedBookingShouldCancelBookingAndRaiseBookingCancelledEvent() {
    // Arrange
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    booking.cancel();

    // Assert
    assertEquals(1, booking.recordedEvents().size());
    assertInstanceOf(BookingCancelled.class, booking.recordedEvents().getFirst());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }

  @Test
  void cancelWithConfirmedBookingShouldThrowBookingNotCancelableException() {
    // Arrange
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(BookingNotCancelableException.class, booking::cancel);
  }

  @Test
  void cancelWithCancelledBookingShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    booking.cancel();

    // Assert
    assertEquals(0, booking.recordedEvents().size());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }
}
