package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.Halls;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.Movies;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ShowTest {

  @Test
  void newShowShouldReturnShowWithSeats() {
    // Arrange
    // Act
    final var show = new Show(
      new ShowId("S0000000000"),
      Instant.EPOCH,
      Movies.newMovie("M0000000000"),
      Halls.newHall("H0000000000"));

    // Assert
    assertEquals(0, show.recordedEvents().size());
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
  void reserveSeatWithAvailableSeatShouldReserveSeatAndRaiseSeatReservedEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");

    // Act
    show.reserveSeat(new BookingId("B0000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(1, show.recordedEvents().size());
    assertInstanceOf(SeatReserved.class, show.recordedEvents().getFirst());
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
    final var seat = reservedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), seat.seatNumber());
    assertEquals(SeatStatus.RESERVED, seat.status());
    assertEquals(new BookingId("B0000000000"), seat.bookingId());
  }

  @Test
  void reserveSeatWithReservedSeatShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");

    // Act
    show.reserveSeat(new BookingId("B0000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.recordedEvents().size());
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
    final var seat = reservedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), seat.seatNumber());
    assertEquals(SeatStatus.RESERVED, seat.status());
    assertEquals(new BookingId("B0000000000"), seat.bookingId());
  }

  @Test
  void reserveSeatWithBookedSeatShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(new BookingId("B0000000000"), new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithInvalidBookingIdAndReservedSeatShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(new BookingId("B0000000001"), new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithInvalidBookingIdAndBookedSeatShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(new BookingId("B0000000001"), new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithAvailableSeatShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");

    // Act
    show.releaseSeat(new BookingId("B0000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.recordedEvents().size());
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
  void releaseSeatWithReservedSeatShouldReleaseSeatAndRaiseSeatReleasedEvent() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");

    // Act
    show.releaseSeat(new BookingId("B0000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(1, show.recordedEvents().size());
    assertInstanceOf(SeatReleased.class, show.recordedEvents().getFirst());
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
  void releaseSeatWithBookedSeatShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(new BookingId("B0000000001"), new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithInvalidBookingIdAndReservedSeatShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(new BookingId("B0000000001"), new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithInvalidBookingIdAndBookedSeatShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(new BookingId("B0000000001"), new SeatNumber("A1")));
  }

  @Test
  void bookSeatsWithNoReservedSeatsShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");

    // Act
    show.bookSeats(new BookingId("B0000000000"));

    // Assert
    assertEquals(0, show.recordedEvents().size());
    assertEquals(new ShowId("S0000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("M0000000000"), show.movie());
    assertEquals(Halls.newHall("H0000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(0, bookedSeats.size());
  }

  @Test
  void bookSeatsWithReservedSeatsShouldBookSeatsAndRaiseSeatBookedEvents() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");

    // Act
    show.bookSeats(new BookingId("B0000000000"));

    // Assert
    assertEquals(2, show.recordedEvents().size());
    assertInstanceOf(SeatBooked.class, show.recordedEvents().getFirst());
    assertInstanceOf(SeatBooked.class, show.recordedEvents().getLast());
    assertEquals(new ShowId("S0000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("M0000000000"), show.movie());
    assertEquals(Halls.newHall("H0000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(2, bookedSeats.size());
    final var firstSeat = bookedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), firstSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, firstSeat.status());
    assertEquals(new BookingId("B0000000000"), firstSeat.bookingId());
    final var lastSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), lastSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, lastSeat.status());
    assertEquals(new BookingId("B0000000000"), lastSeat.bookingId());
  }

  @Test
  void bookSeatsWithBookedSeatsShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");

    // Act
    show.bookSeats(new BookingId("B0000000000"));

    // Assert
    assertEquals(0, show.recordedEvents().size());
    assertEquals(new ShowId("S0000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("M0000000000"), show.movie());
    assertEquals(Halls.newHall("H0000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(2, bookedSeats.size());
    final var firstSeat = bookedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), firstSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, firstSeat.status());
    assertEquals(new BookingId("B0000000000"), firstSeat.bookingId());
    final var lastSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), lastSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, lastSeat.status());
    assertEquals(new BookingId("B0000000000"), lastSeat.bookingId());
  }

  @Test
  void releaseSeatsWithNoReservedSeatsShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");

    // Act
    show.releaseSeats(new BookingId("B0000000000"));

    // Assert
    assertEquals(0, show.recordedEvents().size());
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
  void releaseSeatsWithReservedSeatsShouldReleaseSeatsAndRaiseSeatReleasedEvents() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");

    // Act
    show.releaseSeats(new BookingId("B0000000000"));

    // Assert
    assertEquals(2, show.recordedEvents().size());
    assertInstanceOf(SeatReleased.class, show.recordedEvents().getFirst());
    assertInstanceOf(SeatReleased.class, show.recordedEvents().getLast());
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
  void releaseSeatsWithBookedSeatsShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeats(new BookingId("B0000000000")));
  }
}
