package com.github.booking.application.show;

import com.github.booking.application.show.command.ScheduleShowCommand;
import com.github.booking.domain.hall.HallFixture;
import com.github.booking.domain.hall.HallId;
import com.github.booking.domain.hall.HallService;
import com.github.booking.domain.movie.MovieFixture;
import com.github.booking.domain.movie.MovieId;
import com.github.booking.domain.movie.MovieService;
import com.github.booking.domain.show.Seat;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    when(movieService.movieFrom(new MovieId("M00000000000000000")))
      .thenReturn(MovieFixture.newMovie("M00000000000000000"));
    when(hallService.hallFrom(new HallId("H00000000000000000")))
      .thenReturn(HallFixture.newHall("H00000000000000000"));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var bookingCommandHandler = new ShowCommandHandlerImpl(showRepository, movieService, hallService);
    final var command = new ScheduleShowCommand("S00000000000000000", Instant.EPOCH, "M00000000000000000", "H00000000000000000");

    // Act
    bookingCommandHandler.scheduleShow(command);

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