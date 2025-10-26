package com.github.booking.application.booking;

import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingNotFoundException;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.booking.BookingStatus;
import com.github.booking.domain.booking.Bookings;
import com.github.booking.domain.hall.Halls;
import com.github.booking.domain.movie.Movies;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowNotFoundException;
import com.github.booking.domain.show.ShowRepository;
import com.github.booking.domain.show.Shows;
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
class BookingCommandHandlerImplTest {

  @Mock
  private BookingRepository bookingRepository;
  @Mock
  private ShowRepository showRepository;

  @Test
  void initiateBookingShouldInitiateBookingAndReturnBookingId() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S0000000000")))
      .thenReturn(Optional.of(Shows.newShow("S0000000000")));
    when(bookingRepository.nextBookingId())
      .thenReturn(new BookingId("B0000000000"));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new InitiateBookingCommand("S0000000000");

    // Act
    final var bookingId = bookingCommandHandler.initiateBooking(command);

    // Assert
    assertEquals("B0000000000", bookingId);
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(BookingStatus.INITIATED, booking.status());
  }

  @Test
  void initiateBookingWithUnknownShowIdShouldThrowShowNotFoundException() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S0000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new InitiateBookingCommand("S0000000000");

    // Act
    // Assert
    assertThrows(ShowNotFoundException.class, () -> bookingCommandHandler.initiateBooking(command));
  }

  @Test
  void reserveSeatShouldReserveSeat() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("S0000000000", "B0000000000")));
    when(showRepository.findByShowId(new ShowId("S0000000000")))
      .thenReturn(Optional.of(Shows.newShow("S0000000000")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReserveSeatCommand("B0000000000", "A1");

    // Act
    bookingCommandHandler.reserveSeat(command);

    // Assert
    final var show = showCaptor.getValue();

    assertEquals(new ShowId("S0000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("M0000000000"), show.movie());
    assertEquals(Halls.newHall("H0000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var reservedSeats = seats.stream()
      .filter(Seat::isReserved)
      .toList();

    assertEquals(1, reservedSeats.size());
  }

  @Test
  void reserveSeatWithUnknownBookingIdShouldThrowBookingNotFoundException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReserveSeatCommand("B0000000000", "A1");

    // Act
    // Assert
    assertThrows(BookingNotFoundException.class, () -> bookingCommandHandler.reserveSeat(command));
  }

  @Test
  void releaseSeatShouldReleaseSeat() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("S0000000000", "B0000000000")));
    when(showRepository.findByShowId(new ShowId("S0000000000")))
      .thenReturn(Optional.of(Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeatCommand("B0000000000", "A1");

    // Act
    bookingCommandHandler.releaseSeat(command);

    // Assert
    final var show = showCaptor.getValue();

    assertEquals(new ShowId("S0000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("M0000000000"), show.movie());
    assertEquals(Halls.newHall("H0000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var availableSeats = seats.stream()
      .filter(Seat::isAvailable)
      .toList();

    assertEquals(150, availableSeats.size());
  }

  @Test
  void releaseSeatWithUnknownBookingIdShouldThrowBookingNotFoundException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeatCommand("B0000000000", "A1");

    // Act
    // Assert
    assertThrows(BookingNotFoundException.class, () -> bookingCommandHandler.releaseSeat(command));
  }

  @Test
  void confirmBookingShouldConfirmBooking() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("S0000000000", "B0000000000")));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ConfirmBookingCommand("B0000000000");

    // Act
    bookingCommandHandler.confirmBooking(command);

    // Assert
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmBookingWithUnknownBookingIdShouldThrowBookingNotFoundException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ConfirmBookingCommand("B0000000000");

    // Act
    // Assert
    assertThrows(BookingNotFoundException.class, () -> bookingCommandHandler.confirmBooking(command));
  }

  @Test
  void cancelBookingShouldCancelBooking() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("S0000000000", "B0000000000")));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new CancelBookingCommand("B0000000000");

    // Act
    bookingCommandHandler.cancelBooking(command);

    // Assert
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S0000000000"), booking.showId());
    assertEquals(new BookingId("B0000000000"), booking.bookingId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }

  @Test
  void cancelBookingWithUnknownBookingIdShouldThrowBookingNotFoundException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B0000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new CancelBookingCommand("B0000000000");

    // Act
    // Assert
    assertThrows(BookingNotFoundException.class, () -> bookingCommandHandler.cancelBooking(command));
  }
}
