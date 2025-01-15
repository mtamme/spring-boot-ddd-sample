package com.github.booking.application.booking;

import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.booking.BookingStatus;
import com.github.booking.domain.booking.Bookings;
import com.github.booking.domain.hall.Halls;
import com.github.booking.domain.movie.Movies;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import com.github.booking.domain.show.Shows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingCommandHandlerImplTest {

  @Mock
  private BookingRepository bookingRepository;
  @Mock
  private ShowRepository showRepository;

  @Test
  void initiateBookingShouldInitiateBookingAndReturnBookingId() {
    // Arrange
    Mockito.when(showRepository.findByShowId(new ShowId("40000000000")))
      .thenReturn(Optional.of(Shows.newShow("40000000000")));
    Mockito.when(bookingRepository.nextBookingId())
      .thenReturn(new BookingId("10000000000"));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    Mockito.doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new InitiateBookingCommand("40000000000");

    // Act
    final var bookingId = bookingCommandHandler.initiateBooking(command);

    // Assert
    assertEquals("10000000000", bookingId);
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(BookingStatus.INITIATED, booking.status());
  }

  @Test
  void reserveSeatShouldReserveSeat() {
    // Arrange
    Mockito.when(bookingRepository.findByBookingId(new BookingId("10000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("40000000000", "10000000000")));
    Mockito.when(showRepository.findByShowId(new ShowId("40000000000")))
      .thenReturn(Optional.of(Shows.newShow("40000000000")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    Mockito.doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReserveSeatCommand("10000000000", "A1");

    // Act
    bookingCommandHandler.reserveSeat(command);

    // Assert
    final var show = showCaptor.getValue();

    assertEquals(new ShowId("40000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("30000000000"), show.movie());
    assertEquals(Halls.newHall("20000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var reservedSeats = seats.stream()
      .filter(Seat::isReserved)
      .toList();

    assertEquals(1, reservedSeats.size());
  }

  @Test
  void releaseSeatShouldReleaseSeat() {
    // Arrange
    Mockito.when(bookingRepository.findByBookingId(new BookingId("10000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("40000000000", "10000000000")));
    Mockito.when(showRepository.findByShowId(new ShowId("40000000000")))
      .thenReturn(Optional.of(Shows.newShowWithReservedSeats("40000000000", "10000000000", "A1")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    Mockito.doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ReleaseSeatCommand("10000000000", "A1");

    // Act
    bookingCommandHandler.releaseSeat(command);

    // Assert
    final var show = showCaptor.getValue();

    assertEquals(new ShowId("40000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("30000000000"), show.movie());
    assertEquals(Halls.newHall("20000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var availableSeats = seats.stream()
      .filter(Seat::isAvailable)
      .toList();

    assertEquals(150, availableSeats.size());
  }

  @Test
  void confirmBookingShouldConfirmBooking() {
    // Arrange
    Mockito.when(bookingRepository.findByBookingId(new BookingId("10000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("40000000000", "10000000000")));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    Mockito.doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new ConfirmBookingCommand("10000000000");

    // Act
    bookingCommandHandler.confirmBooking(command);

    // Assert
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(BookingStatus.CONFIRMED, booking.status());
  }

  @Test
  void cancelBookingShouldCancelBooking() {
    // Arrange
    Mockito.when(bookingRepository.findByBookingId(new BookingId("10000000000")))
      .thenReturn(Optional.of(Bookings.newInitiatedBooking("40000000000", "10000000000")));
    final var bookingCaptor = ArgumentCaptor.forClass(Booking.class);

    Mockito.doNothing()
      .when(bookingRepository)
      .save(bookingCaptor.capture());
    final var bookingCommandHandler = new BookingCommandHandlerImpl(bookingRepository, showRepository);
    final var command = new CancelBookingCommand("10000000000");

    // Act
    bookingCommandHandler.cancelBooking(command);

    // Assert
    final var booking = bookingCaptor.getValue();

    assertEquals(new ShowId("40000000000"), booking.showId());
    assertEquals(new BookingId("10000000000"), booking.bookingId());
    assertEquals(BookingStatus.CANCELLED, booking.status());
  }
}
