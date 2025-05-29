package com.github.booking.application.show;

import com.github.booking.domain.booking.BookingCancelled;
import com.github.booking.domain.booking.BookingConfirmed;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.hall.HallFixture;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.MovieFixture;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.SeatStatus;
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
class ShowEventHandlerTest {

  @Mock
  private ShowRepository showRepository;
  @Mock
  private BookingRepository bookingRepository;

  @Test
  void onBookingConfirmedShouldBookSeats() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2")));
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var showEventHandler = new ShowEventHandler(showRepository, bookingRepository);
    final var event = new BookingConfirmed(new ShowId("S00000000000000000"), new BookingId("B00000000000000000"));

    // Act
    showEventHandler.onBookingConfirmed(event);

    // Assert
    final var show = showCaptor.getValue();

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
    final var secondSeat = bookedSeats.getLast();

    assertEquals(new SeatNumber("A2"), secondSeat.seatNumber());
    assertEquals(SeatStatus.BOOKED, secondSeat.status());
    assertEquals(new BookingId("B00000000000000000"), secondSeat.bookingId());
  }

  @Test
  void onBookingCancelledShouldReleaseSeats() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShowWithReservedSeats("S00000000000000000", "B00000000000000000", "A1", "A2")));
    when(bookingRepository.findByBookingId(new BookingId("B00000000000000000")))
      .thenReturn(Optional.of(BookingFixture.newCancelledBooking("S00000000000000000", "B00000000000000000")));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var showEventHandler = new ShowEventHandler(showRepository, bookingRepository);
    final var event = new BookingCancelled(new ShowId("S00000000000000000"), new BookingId("B00000000000000000"));

    // Act
    showEventHandler.onBookingCancelled(event);

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
}
