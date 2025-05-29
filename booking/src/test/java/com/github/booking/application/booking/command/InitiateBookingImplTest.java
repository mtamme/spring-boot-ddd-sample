package com.github.booking.application.booking.command;

import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.booking.BookingStatus;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowFixture;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitiateBookingImplTest {

  @Mock
  private BookingRepository bookingRepository;
  @Mock
  private ShowRepository showRepository;

  @Test
  void handleShouldInitiateBookingAndReturnBookingId() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShow("S00000000000000000")));
    when(bookingRepository.nextBookingId())
      .thenReturn(new BookingId("B00000000000000000"));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var handler = new InitiateBookingImpl(bookingRepository, showRepository);
    final var command = new InitiateBooking.Command("S00000000000000000");

    // Act
    final var result = handler.handle(command);

    // Assert
    assertEquals("B00000000000000000", result.bookingId());
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(BookingStatus.INITIATED, booking.status());
  }

  @Test
  void handleWithUnknownShowIdShouldThrowShowException() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.empty());
    final var handler = new InitiateBookingImpl(bookingRepository, showRepository);
    final var command = new InitiateBooking.Command("S00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(ShowException.class, () -> handler.handle(command));

    assertEquals(ShowException.NOT_FOUND_PROBLEM, exception.getProblem());
  }
}
