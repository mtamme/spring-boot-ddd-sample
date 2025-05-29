package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingFixture;
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
  void reserveSeatWithAvailableSeatAndInitiatedBookingShouldReserveSeatAndRaiseSeatReservedEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.reserveSeat(booking, new SeatNumber("A1"));

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
  void reserveSeatWithAvailableSeatAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithAvailableSeatAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithReservedSeatAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.reserveSeat(booking, new SeatNumber("A1"));

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
  void reserveSeatWithReservedSeatAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithReservedSeatAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithReservedSeatAndUnassignedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000001");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithBookedSeatAndInitiatedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithBookedSeatAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithBookedSeatAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void reserveSeatWithBookedSeatAndUnassignedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000001");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RESERVABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithAvailableSeatAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

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
  void releaseSeatWithAvailableSeatAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithAvailableSeatAndCancelledBookingShouldReleaseSeat() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

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
  void releaseSeatWithReservedSeatAndInitiatedBookingShouldReleaseSeatAndRaiseSeatReleasedEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

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
  void releaseSeatWithReservedSeatAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithReservedSeatAndCancelledBookingShouldReleaseSeatAndRaiseSeatReleasedEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

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
  void releaseSeatWithReservedSeatAndUnassignedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000001");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithBookedSeatAndInitiatedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000001");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithBookedSeatAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithBookedSeatAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatWithBookedSeatAndUnassignedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000001");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void bookSeatsWithNoReservedSeatsAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.bookSeats(booking);

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
  void bookSeatsWithNoReservedSeatsAndConfirmedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.bookSeats(booking);

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
  void bookSeatsWithNoReservedSeatsAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.bookSeats(booking));

    assertEquals(SeatException.NOT_BOOKABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void bookSeatsWithReservedSeatsAndInitiatedBookingShouldBookSeatsAndRaiseSeatBookedEvents() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.bookSeats(booking);

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
  void bookSeatsWithReservedSeatsAndConfirmedBookingShouldBookSeatsAndRaiseSeatBookedEvents() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.bookSeats(booking);

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
  void bookSeatsWithReservedSeatsAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.bookSeats(booking));

    assertEquals(SeatException.NOT_BOOKABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void bookSeatsWithBookedSeatsAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.bookSeats(booking);

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
  void bookSeatsWithBookedSeatsAndConfirmedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.bookSeats(booking);

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
  void bookSeatsWithBookedSeatsAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.bookSeats(booking));

    assertEquals(SeatException.NOT_BOOKABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatsWithNoReservedSeatsAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeats(booking);

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
  void releaseSeatsWithNoReservedSeatsAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeats(booking));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatsWithNoReservedSeatsAndCancelledBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = ShowFixture.newShow("S00000000000000000");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeats(booking);

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
  void releaseSeatsWithReservedSeatsAndInitiatedBookingShouldReleaseSeatsAndRaiseSeatReleasedEvents() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeats(booking);

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
  void releaseSeatsWithReservedSeatsAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeats(booking));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatsWithReservedSeatsAndCancelledBookingShouldReleaseSeatsAndRaiseSeatReleasedEvents() {
    // Arrange
    final var show = ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    show.releaseSeats(booking);

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
  void releaseSeatsWithBookedSeatsAndInitiatedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newInitiatedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeats(booking));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatsWithBookedSeatsAndConfirmedBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeats(booking));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }

  @Test
  void releaseSeatsWithBookedSeatsAndCancelledBookingShouldThrowSeatException() {
    // Arrange
    final var show = ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1", "A2");
    final var booking = BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(SeatException.class, () -> show.releaseSeats(booking));

    assertEquals(SeatException.NOT_RELEASEABLE_PROBLEM, exception.getProblem());
  }
}
