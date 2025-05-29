package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

  @Test
  void newBookingShouldReturnInitiatedBookingAndRaiseBookingInitiatedEvent() {
    // Arrange
    // Act
    final var booking = new Booking(new ShowId("S00000000000000000"), new BookingId("B00000000000000000"));

    // Assert
    assertEquals(1, booking.raisedEvents().size());
    assertInstanceOf(BookingInitiated.class, booking.raisedEvents().getFirst());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(BookingStatus.INITIATED, booking.status());
  }

  @Test
  void confirmWithInitiatedBookingShouldConfirmBookingAndRaiseBookingConfirmedEvent() {
    // Arrange
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    booking.confirm();

    // Assert
    assertEquals(1, booking.raisedEvents().size());
    assertInstanceOf(BookingConfirmed.class, booking.raisedEvents().getFirst());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmWithConfirmedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    booking.confirm();

    // Assert
    assertEquals(0, booking.raisedEvents().size());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmWithCancelledBookingShouldThrowBookingException() {
    // Arrange
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, booking::confirm);

    assertEquals(BookingException.NOT_CONFIRMABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void cancelWithInitiatedBookingShouldCancelBookingAndRaiseBookingCancelledEvent() {
    // Arrange
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    booking.cancel();

    // Assert
    assertEquals(1, booking.raisedEvents().size());
    assertInstanceOf(BookingCancelled.class, booking.raisedEvents().getFirst());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }

  @Test
  void cancelWithConfirmedBookingShouldThrowBookingException() {
    // Arrange
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, booking::cancel);

    assertEquals(BookingException.NOT_CANCELABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void cancelWithCancelledBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    booking.cancel();

    // Assert
    assertEquals(0, booking.raisedEvents().size());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }
}
