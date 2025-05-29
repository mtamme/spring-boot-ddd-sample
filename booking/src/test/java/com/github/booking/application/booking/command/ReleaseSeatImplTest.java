package com.github.booking.application.booking.command;

import com.github.booking.domain.booking.BookingException;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.hall.HallFixture;
import com.github.booking.domain.movie.MovieFixture;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowFixture;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseSeatImplTest {

  @Mock
  private BookingRepository bookingRepository;
  @Mock
  private ShowRepository showRepository;

  @Test
  void handleWithInitiatedBookingShouldReleaseSeat() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000")));
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var handler = new ReleaseSeatImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeat.Command("B00000000000000000", "A1");

    // Act
    handler.handle(command);

    // Assert
    final var show = showCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(MovieFixture.newMovie("M00000000000000000"), show.movie());
    assertEquals(HallFixture.newHall("H00000000000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var availableSeats = seats.stream()
      .filter(Seat::isAvailable)
      .toList();

    assertEquals(150, availableSeats.size());
  }

  @Test
  void handleWithConfirmedBookingShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000")));
    final var handler = new ReleaseSeatImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeat.Command("B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> handler.handle(command));

    assertEquals(BookingException.NOT_INITIATED_PROBLEM, exception.getProblem());
  }

  @Test
  void handleWithCancelledBookingShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000")));
    final var handler = new ReleaseSeatImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeat.Command("B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> handler.handle(command));

    assertEquals(BookingException.NOT_INITIATED_PROBLEM, exception.getProblem());
  }

  @Test
  void handleWithUnknownBookingIdShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.empty());
    final var handler = new ReleaseSeatImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeat.Command("B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> handler.handle(command));

    assertEquals(BookingException.NOT_FOUND_PROBLEM, exception.getProblem());
  }
}
