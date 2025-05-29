package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.HallFixture;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.MovieFixture;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ShowTest {

  @Test
  void newShowShouldReturnShowWithSeats() {
    // Arrange
    // Act
    final var show = new Show(
      new ShowId("S00000000000000000"),
      Instant.EPOCH,
      MovieFixture.newMovie("M00000000000000000"),
      HallFixture.newHall("H00000000000000000"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void reserveSeatWithAvailableSeatShouldReserveSeatAndRaiseSeatReservedEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");

    // Act
    show.reserveSeat(new BookingId("B00000000000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(1, show.raisedEvents().size());
    assertInstanceOf(SeatReserved.class, show.raisedEvents().getFirst());
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
    final var seat = reservedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), seat.seatNumber());
    assertEquals(SeatStatus.RESERVED, seat.status());
    assertEquals(new BookingId("B00000000000000000"), seat.bookingId());
  }

  @Test
  void reserveSeatWithReservedSeatShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    show.reserveSeat(new BookingId("B00000000000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
    final var seat = reservedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), seat.seatNumber());
    assertEquals(SeatStatus.RESERVED, seat.status());
    assertEquals(new BookingId("B00000000000000000"), seat.bookingId());
  }

  @Test
  void reserveSeatWithReservedSeatAndUnassignedBookingIdShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(new BookingId("B00000000000000001"), new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithBookedSeatShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(new BookingId("B00000000000000000"), new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithBookedSeatAndUnassignedBookingIdShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(new BookingId("B00000000000000001"), new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithAvailableSeatShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");

    // Act
    show.releaseSeat(new BookingId("B00000000000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void releaseSeatWithReservedSeatShouldReleaseSeatAndRaiseSeatReleasedEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    show.releaseSeat(new BookingId("B00000000000000000"), new SeatNumber("A1"));

    // Assert
    assertEquals(1, show.raisedEvents().size());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getFirst());
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
  void releaseSeatWithReservedSeatAndUnassignedBookingIdShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(new BookingId("B00000000000000001"), new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithBookedSeatShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(new BookingId("B00000000000000000"), new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithBookedSeatAndUnassignedBookingIdShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(new BookingId("B00000000000000001"), new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void bookSeatsWithNoReservedSeatsShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");

    // Act
    show.bookSeats(new BookingId("B00000000000000000"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
    assertEquals(new ShowId("S00000000000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(MovieFixture.newMovie("M00000000000000000"), show.movie());
    assertEquals(HallFixture.newHall("H00000000000000000"), show.hall());
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
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");

    // Act
    show.bookSeats(new BookingId("B00000000000000000"));

    // Assert
    assertEquals(2, show.raisedEvents().size());
    assertInstanceOf(SeatBooked.class, show.raisedEvents().getFirst());
    assertInstanceOf(SeatBooked.class, show.raisedEvents().getLast());
    assertEquals(new ShowId("S00000000000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(MovieFixture.newMovie("M00000000000000000"), show.movie());
    assertEquals(HallFixture.newHall("H00000000000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(2, bookedSeats.size());
    final var firstSeat = bookedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), firstSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, firstSeat.status());
    assertEquals(new BookingId("B00000000000000000"), firstSeat.bookingId());
    final var lastSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), lastSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, lastSeat.status());
    assertEquals(new BookingId("B00000000000000000"), lastSeat.bookingId());
  }

  @Test
  void bookSeatsWithBookedSeatsShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");

    // Act
    show.bookSeats(new BookingId("B00000000000000000"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
    assertEquals(new ShowId("S00000000000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(MovieFixture.newMovie("M00000000000000000"), show.movie());
    assertEquals(HallFixture.newHall("H00000000000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(2, bookedSeats.size());
    final var firstSeat = bookedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), firstSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, firstSeat.status());
    assertEquals(new BookingId("B00000000000000000"), firstSeat.bookingId());
    final var lastSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), lastSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, lastSeat.status());
    assertEquals(new BookingId("B00000000000000000"), lastSeat.bookingId());
  }

  @Test
  void releaseSeatsWithNoReservedSeatsShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");

    // Act
    show.releaseSeats(new BookingId("B00000000000000000"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void releaseSeatsWithReservedSeatsShouldReleaseSeatsAndRaiseSeatReleasedEvents() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");

    // Act
    show.releaseSeats(new BookingId("B00000000000000000"));

    // Assert
    assertEquals(2, show.raisedEvents().size());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getFirst());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getLast());
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
  void releaseSeatsWithBookedSeatsShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeats(new BookingId("B00000000000000000")));

    assertEquals(SeatException.NOT_RELEASABLE_PROBLEM, exception.getProblem());
  }
}
