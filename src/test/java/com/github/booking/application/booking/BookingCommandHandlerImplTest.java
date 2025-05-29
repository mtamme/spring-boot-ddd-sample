package com.github.booking.application.booking;

import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingException;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.booking.BookingStatus;
import com.github.booking.domain.hall.HallFixture;
import com.github.booking.domain.movie.MovieFixture;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowException;
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
class BookingCommandHandlerImplTest {

  @Mock
  private BookingRepository bookingRepository;
  @Mock
  private ShowRepository showRepository;

  @Test
  void initiateBookingShouldInitiateBookingAndReturnBookingId() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShow("S00000000000000000")));
    when(bookingRepository.nextBookingId())
      .thenReturn(new BookingId("B00000000000000000"));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new InitiateBookingCommand("S00000000000000000");

    // Act
    final var result = bookingCommandHandler.initiateBooking(command);

    // Assert
    assertEquals("B00000000000000000", result.bookingId());
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(BookingStatus.INITIATED, booking.status());
  }

  @Test
  void initiateBookingWithUnknownShowIdShouldThrowShowException() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new InitiateBookingCommand("S00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(ShowException.class, () -> bookingCommandHandler.initiateBooking(command));

    assertEquals(ShowException.NOT_FOUND_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatShouldReserveSeat() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000")));
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShow("S00000000000000000")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReserveSeatCommand("B00000000000000000", "A1");

    // Act
    bookingCommandHandler.reserveSeat(command);

    // Assert
    final var show = showCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(MovieFixture.newMovie("M00000000000000000"), show.movie());
    assertEquals(HallFixture.newHall("H00000000000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var reservedSeats = seats.stream()
      .filter(Seat::isReserved)
      .toList();

    assertEquals(1, reservedSeats.size());
  }

  @Test
  void reserveSeatWithUnknownBookingIdShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReserveSeatCommand("B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> bookingCommandHandler.reserveSeat(command));

    assertEquals(BookingException.NOT_FOUND_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatShouldReleaseSeat() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000")));
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeatCommand("B00000000000000000", "A1");

    // Act
    bookingCommandHandler.releaseSeat(command);

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
  void releaseSeatWithUnknownBookingIdShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeatCommand("B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> bookingCommandHandler.releaseSeat(command));

    assertEquals(BookingException.NOT_FOUND_PROBLEM, exception.getProblem());
  }

  @Test
  void confirmBookingShouldConfirmBooking() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000")));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ConfirmBookingCommand("B00000000000000000");

    // Act
    bookingCommandHandler.confirmBooking(command);

    // Assert
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void confirmBookingWithUnknownBookingIdShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ConfirmBookingCommand("B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> bookingCommandHandler.confirmBooking(command));

    assertEquals(BookingException.NOT_FOUND_PROBLEM, exception.getProblem());
  }

  @Test
  void cancelBookingShouldCancelBooking() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000")));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new CancelBookingCommand("B00000000000000000");

    // Act
    bookingCommandHandler.cancelBooking(command);

    // Assert
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), booking.showId());
    assertEquals(new BookingId("B00000000000000000"), booking.bookingId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }

  @Test
  void cancelBookingWithUnknownBookingIdShouldThrowBookingException() {
    // Arrange
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.empty());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new CancelBookingCommand("B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(BookingException.class, () -> bookingCommandHandler.cancelBooking(command));

    assertEquals(BookingException.NOT_FOUND_PROBLEM, exception.getProblem());
  }
}
