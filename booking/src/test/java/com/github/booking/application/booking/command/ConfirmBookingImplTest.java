package com.github.booking.application.booking.command;

import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingException;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.booking.BookingStatus;
import com.github.booking.domain.show.ShowId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmBookingImplTest {

  @Mock
  private BookingRepository bookingRepository;

  @Test
  void handleShouldConfirmBooking() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000")));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var handler = new ConfirmBookingImpl(bookingRepository);
    final var command = new ConfirmBooking.Command("B00000000000000000");

    // Act
    handler.handle(command);

    // Assert
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void handleWithUnknownBookingIdShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.empty());
    final var handler = new ConfirmBookingImpl(bookingRepository);
    final var command = new ConfirmBooking.Command("B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> handler.handle(command));

    assertEquals(BookingException.NOT_FOUND_PROBLEM, exception.getProblem());
  }
}
