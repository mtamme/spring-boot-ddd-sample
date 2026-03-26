package com.github.scheduling.application.show;

import com.github.scheduling.application.show.command.ScheduleShowCommand;
import com.github.scheduling.domain.hall.HallFixture;
import com.github.scheduling.domain.hall.HallId;
import com.github.scheduling.domain.hall.HallService;
import com.github.scheduling.domain.movie.MovieFixture;
import com.github.scheduling.domain.movie.MovieId;
import com.github.scheduling.domain.movie.MovieService;
import com.github.scheduling.domain.show.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

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
  @Mock
  private ShowSchedulingPolicy showSchedulingPolicy;

  private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @Test
  void scheduleShowShouldScheduleShow() {
    // Arrange
    final var scheduledAt = Instant.parse("2026-01-08T00:00:00Z");

    when(showRepository.nextShowId())
      .thenReturn(new ShowId("S00000000000000000"));
    when(movieService.movieFrom(new MovieId("M00000000000000000")))
      .thenReturn(MovieFixture.newMovie("M00000000000000000"));
    when(hallService.hallFrom(new HallId("H00000000000000000")))
      .thenReturn(HallFixture.newHall("H00000000000000000"));
    final var showCaptor = ArgumentCaptor.forClass(Show.class);

    doNothing()
      .when(showRepository)
      .save(showCaptor.capture());
    final var handler = new ShowCommandHandlerImpl(showRepository, movieService, hallService, showSchedulingPolicy, clock);
    final var command = new ScheduleShowCommand(scheduledAt, "M00000000000000000", "H00000000000000000");

    // Act
    final var result = handler.scheduleShow(command);

    // Assert
    final var show = showCaptor.getValue();

    assertEquals(new ShowId("S00000000000000000"), show.showId());
    assertEquals(scheduledAt, show.scheduledAt());
    assertEquals(MovieFixture.newMovie("M00000000000000000"), show.movie());
    assertEquals(HallFixture.newHall("H00000000000000000"), show.hall());
    assertEquals("S00000000000000000", result.showId());

    verify(showSchedulingPolicy).ensureNoOverlap(
      eq(new HallId("H00000000000000000")),
      eq(scheduledAt),
      eq(scheduledAt.plus(120L, ChronoUnit.MINUTES)));
  }

  @Test
  void scheduleShowWithOverlapShouldThrowShowException() {
    // Arrange
    final var scheduledAt = Instant.parse("2026-01-08T00:00:00Z");

    when(movieService.movieFrom(new MovieId("M00000000000000000")))
      .thenReturn(MovieFixture.newMovie("M00000000000000000"));
    when(hallService.hallFrom(new HallId("H00000000000000000")))
      .thenReturn(HallFixture.newHall("H00000000000000000"));
    doThrow(ShowException.overlap())
      .when(showSchedulingPolicy)
      .ensureNoOverlap(any(), any(), any());
    final var handler = new ShowCommandHandlerImpl(showRepository, movieService, hallService, showSchedulingPolicy, clock);
    final var command = new ScheduleShowCommand(scheduledAt, "M00000000000000000", "H00000000000000000");

    // Act & Assert
    assertThrows(ShowException.class, () -> handler.scheduleShow(command));
    verify(showRepository, never()).save(any());
  }
}
