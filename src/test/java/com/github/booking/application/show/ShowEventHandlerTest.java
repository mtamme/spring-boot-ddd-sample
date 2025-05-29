package com.github.booking.application.show;

import com.github.booking.domain.booking.BookingCancelled;
import com.github.booking.domain.booking.BookingConfirmed;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.Halls;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.Movies;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.SeatStatus;
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
class ShowEventHandlerTest {

  @Mock
  private ShowRepository showRepository;

  @Test
  void onBookingConfirmedShouldBookSeats() {
    // Arrange
    Mockito.when(showRepository.findByShowId(new ShowId("S0000000000")))
      .thenReturn(Optional.of(Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    Mockito.doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var showEventHandler = new ShowEventHandler(showRepository);
    final var event = new BookingConfirmed(new ShowId("S0000000000"), new BookingId("B0000000000"));

    // Act
    showEventHandler.onBookingConfirmed(event);

    // Assert
    final var show = showCaptor.getValue();

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
    final var secondSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), secondSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, secondSeat.status());
    assertEquals(new BookingId("B0000000000"), secondSeat.bookingId());
  }

  @Test
  void onBookingCancelledShouldReleaseSeats() {
    // Arrange
    Mockito.when(showRepository.findByShowId(new ShowId("S0000000000")))
      .thenReturn(Optional.of(Shows.newShowWithReservedSeats("S0000000000", "B0000000000", "A1", "A2")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    Mockito.doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var showEventHandler = new ShowEventHandler(showRepository);
    final var event = new BookingCancelled(new ShowId("S0000000000"), new BookingId("B0000000000"));

    // Act
    showEventHandler.onBookingCancelled(event);

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
}
