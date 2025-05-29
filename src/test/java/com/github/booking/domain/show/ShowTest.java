package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.Bookings;
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
    assertEquals(0, show.raisedEvents().size());
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
  void reserveSeatWithAvailableSeatAndInitiatedBookingShouldReserveSeatAndRaiseSeatReservedEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.reserveSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(1, show.raisedEvents().size());
    assertInstanceOf(SeatReserved.class, show.raisedEvents().getFirst());
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
  void reserveSeatWithAvailableSeatAndConfirmedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithAvailableSeatAndCancelledBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithReservedSeatAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.reserveSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void reserveSeatWithReservedSeatAndConfirmedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithReservedSeatAndCancelledBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithReservedSeatAndUnassignedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000001");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithBookedSeatAndInitiatedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithBookedSeatAndConfirmedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithBookedSeatAndCancelledBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithBookedSeatAndUnassignedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000001");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithAvailableSeatAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void releaseSeatWithAvailableSeatAndConfirmedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithAvailableSeatAndCancelledBookingShouldReleaseSeat() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void releaseSeatWithReservedSeatAndInitiatedBookingShouldReleaseSeatAndRaiseSeatReleasedEvent() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(1, show.raisedEvents().size());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getFirst());
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
  void releaseSeatWithReservedSeatAndConfirmedBookingShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithReservedSeatAndCancelledBookingShouldReleaseSeatAndRaiseSeatReleasedEvent() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(1, show.raisedEvents().size());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getFirst());
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
  void releaseSeatWithReservedSeatAndUnassignedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000001");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithBookedSeatAndInitiatedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000001");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithBookedSeatAndConfirmedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithBookedSeatAndCancelledBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithBookedSeatAndUnassignedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000001");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void bookSeatsWithNoReservedSeatsAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void bookSeatsWithNoReservedSeatsAndConfirmedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void bookSeatsWithNoReservedSeatsAndCancelledBookingShouldThrowSeatNotBookableException() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotBookableException.class, () -> show.bookSeats(booking));
  }

  @Test
  void bookSeatsWithReservedSeatsAndInitiatedBookingShouldBookSeatsAndRaiseSeatBookedEvents() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(2, show.raisedEvents().size());
    assertInstanceOf(SeatBooked.class, show.raisedEvents().getFirst());
    assertInstanceOf(SeatBooked.class, show.raisedEvents().getLast());
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
  void bookSeatsWithReservedSeatsAndConfirmedBookingShouldBookSeatsAndRaiseSeatBookedEvents() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(2, show.raisedEvents().size());
    assertInstanceOf(SeatBooked.class, show.raisedEvents().getFirst());
    assertInstanceOf(SeatBooked.class, show.raisedEvents().getLast());
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
  void bookSeatsWithReservedSeatsAndCancelledBookingShouldThrowSeatNotBookableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotBookableException.class, () -> show.bookSeats(booking));
  }

  @Test
  void bookSeatsWithBookedSeatsAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void bookSeatsWithBookedSeatsAndConfirmedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void bookSeatsWithBookedSeatsAndCancelledBookingShouldThrowSeatNotBookableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotBookableException.class, () -> show.bookSeats(booking));
  }

  @Test
  void releaseSeatsWithNoReservedSeatsAndInitiatedBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeats(booking);

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void releaseSeatsWithNoReservedSeatsAndConfirmedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeats(booking));
  }

  @Test
  void releaseSeatsWithNoReservedSeatsAndCancelledBookingShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var show = Shows.newShow("S0000000000");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeats(booking);

    // Assert
    assertEquals(0, show.raisedEvents().size());
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
  void releaseSeatsWithReservedSeatsAndInitiatedBookingShouldReleaseSeatsAndRaiseSeatReleasedEvents() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeats(booking);

    // Assert
    assertEquals(2, show.raisedEvents().size());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getFirst());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getLast());
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
  void releaseSeatsWithReservedSeatsAndConfirmedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeats(booking));
  }

  @Test
  void releaseSeatsWithReservedSeatsAndCancelledBookingShouldReleaseSeatsAndRaiseSeatReleasedEvents() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    show.releaseSeats(booking);

    // Assert
    assertEquals(2, show.raisedEvents().size());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getFirst());
    assertInstanceOf(SeatReleased.class, show.raisedEvents().getLast());
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
  void releaseSeatsWithBookedSeatsAndInitiatedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newInitiatedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeats(booking));
  }

  @Test
  void releaseSeatsWithBookedSeatsAndConfirmedBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeats(booking));
  }

  @Test
  void releaseSeatsWithBookedSeatsAndCancelledBookingShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("S0000000000", "B0000000000", "A1", "A2");
    final var booking = Bookings.newCancelledBooking("S0000000000", "B0000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeats(booking));
  }
}
