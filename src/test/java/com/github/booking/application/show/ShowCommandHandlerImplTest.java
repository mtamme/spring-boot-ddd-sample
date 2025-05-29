package com.github.booking.application.show;

import com.github.booking.application.show.command.ScheduleShowCommand;
import com.github.booking.domain.hall.HallId;
import com.github.booking.domain.hall.HallService;
import com.github.booking.domain.hall.Halls;
import com.github.booking.domain.movie.MovieId;
import com.github.booking.domain.movie.MovieService;
import com.github.booking.domain.movie.Movies;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ShowCommandHandlerImplTest {

  @Mock
  private ShowRepository showRepository;
  @Mock
  private MovieService movieService;
  @Mock
  private HallService hallService;

  @Test
  void scheduleShowShouldScheduleShowAndReturnBookingId() {
    // Arrange
    Mockito.when(movieService.movieFrom(new MovieId("M0000000000")))
      .thenReturn(Movies.newMovie("M0000000000"));
    Mockito.when(hallService.hallFrom(new HallId("H0000000000")))
      .thenReturn(Halls.newHall("H0000000000"));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    Mockito.doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new ShowCommandHandlerImpl(showRepository, movieService, hallService);
    final var command = new ScheduleShowCommand("S0000000000", Instant.EPOCH, "M0000000000", "H0000000000");

    // Act
    bookingCommandHandler.scheduleShow(command);

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