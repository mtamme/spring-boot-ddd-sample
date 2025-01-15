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
      new ShowId("40000000000"),
      Instant.EPOCH,
      Movies.newMovie("30000000000"),
      Halls.newHall("20000000000"));

    // Assert
    assertEquals(0, show.events().size());
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
  void reserveSeatWithAvailableSeatShouldReserveSeat() {
    // Arrange
    final var show = Shows.newShow("40000000000");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

    // Act
    show.reserveSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.events().size());
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
    final var seat = reservedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), seat.seatNumber());
    assertEquals(SeatStatus.RESERVED, seat.status());
    assertEquals(new BookingId("10000000000"), seat.bookingId());
  }

  @Test
  void reserveSeatWithReservedSeatShouldDoNothing() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

    // Act
    show.reserveSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.events().size());
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
    final var seat = reservedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), seat.seatNumber());
    assertEquals(SeatStatus.RESERVED, seat.status());
    assertEquals(new BookingId("10000000000"), seat.bookingId());
  }

  @Test
  void reserveSeatWithBookedSeatShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000001");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithInvalidBookingAndReservedSeatShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000001");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void reserveSeatWithInvalidBookingAndBookedSeatShouldThrowSeatNotReservableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000001");

    // Act
    // Assert
    assertThrows(SeatNotReservableException.class, () -> show.reserveSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithAvailableSeatShouldDoNothing() {
    // Arrange
    final var show = Shows.newShow("40000000000");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.events().size());
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
  void releaseSeatWithReservedSeatShouldReleaseSeat() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000000");

    // Act
    show.releaseSeat(booking, new SeatNumber("A1"));

    // Assert
    assertEquals(0, show.events().size());
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
  void releaseSeatWithBookedSeatShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000001");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithInvalidBookingAndReservedSeatShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000001");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void releaseSeatWithInvalidBookingAndBookedSeatShouldThrowSeatNotReleasableException() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("40000000000", "10000000000", "A1");
    final var booking = Bookings.newInitiatedBooking("40000000000", "10000000001");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeat(booking, new SeatNumber("A1")));
  }

  @Test
  void bookSeatsWithNoReservedSeatsShouldDoNothing() {
    // Arrange
    final var show = Shows.newShow("40000000000");
    final var booking = Bookings.newConfirmedBooking("40000000000", "10000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(0, show.events().size());
    assertEquals(new ShowId("40000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("30000000000"), show.movie());
    assertEquals(Halls.newHall("20000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(0, bookedSeats.size());
  }

  @Test
  void bookSeatsWithReservedSeatsShouldBookSeats() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("40000000000", "10000000000", "A1", "A2");
    final var booking = Bookings.newConfirmedBooking("40000000000", "10000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(0, show.events().size());
    assertEquals(new ShowId("40000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("30000000000"), show.movie());
    assertEquals(Halls.newHall("20000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(2, bookedSeats.size());
    final var firstSeat = bookedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), firstSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, firstSeat.status());
    assertEquals(new BookingId("10000000000"), firstSeat.bookingId());
    final var secondSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), secondSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, secondSeat.status());
    assertEquals(new BookingId("10000000000"), secondSeat.bookingId());
  }

  @Test
  void bookSeatsWithBookedSeatsShouldDoNothing() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("40000000000", "10000000000", "A1", "A2");
    final var booking = Bookings.newConfirmedBooking("40000000000", "10000000000");

    // Act
    show.bookSeats(booking);

    // Assert
    assertEquals(0, show.events().size());
    assertEquals(new ShowId("40000000000"), show.showId());
    assertEquals(Instant.EPOCH, show.scheduledAt());
    assertEquals(Movies.newMovie("30000000000"), show.movie());
    assertEquals(Halls.newHall("20000000000"), show.hall());
    final var seats = show.seats();

    assertEquals(150, seats.size());
    final var bookedSeats = seats.stream()
      .filter(Seat::isBooked)
      .toList();

    assertEquals(2, bookedSeats.size());
    final var firstSeat = bookedSeats.getFirst();

    assertEquals(new SeatNumber("A1"), firstSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, firstSeat.status());
    assertEquals(new BookingId("10000000000"), firstSeat.bookingId());
    final var secondSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), secondSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, secondSeat.status());
    assertEquals(new BookingId("10000000000"), secondSeat.bookingId());
  }

  @Test
  void releaseSeatsWithNoReservedSeatsShouldDoNothing() {
    // Arrange
    final var show = Shows.newShow("40000000000");
    final var booking = Bookings.newCancelledBooking("40000000000", "10000000000");

    // Act
    show.releaseSeats(booking);

    // Assert
    assertEquals(0, show.events().size());
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
  void releaseSeatsWithReservedSeatsShouldReleaseSeats() {
    // Arrange
    final var show = Shows.newShowWithReservedSeats("40000000000", "10000000000", "A1", "A2");
    final var booking = Bookings.newCancelledBooking("40000000000", "10000000000");

    // Act
    show.releaseSeats(booking);

    // Assert
    assertEquals(0, show.events().size());
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
  void releaseSeatsWithBookedSeatsShouldReleaseSeats() {
    // Arrange
    final var show = Shows.newShowWithBookedSeats("40000000000", "10000000000", "A1", "A2");
    final var booking = Bookings.newCancelledBooking("40000000000", "10000000000");

    // Act
    // Assert
    assertThrows(SeatNotReleasableException.class, () -> show.releaseSeats(booking));
  }
}
