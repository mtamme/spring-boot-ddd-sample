package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.HallFixture;
import com.github.scheduling.domain.movie.MovieFixture;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ShowTest {

  @Test
  void constructorShouldCreateShowAndRaiseShowScheduledEvent() {
    // Arrange
    final var showId = new ShowId("S00000000000000000");
    final var scheduledAt = Instant.now().plus(7L, ChronoUnit.DAYS);
    final var movie = MovieFixture.newMovie("M00000000000000000");
    final var hall = HallFixture.newHall("H00000000000000000");

    // Act
    final var show = new Show(showId, scheduledAt, movie, hall);

    // Assert
    assertEquals(showId, show.showId());
    assertEquals(scheduledAt, show.scheduledAt());
    assertEquals(movie, show.movie());
    assertEquals(hall, show.hall());
    assertEquals(1, show.raisedEvents().size());

    final var event = (ShowScheduled) show.raisedEvents().getFirst();

    assertEquals(showId, event.showId());
    assertEquals(movie.movieId(), event.movieId());
    assertEquals(hall.hallId(), event.hallId());
    assertEquals(scheduledAt, event.scheduledAt());
  }

  @Test
  void constructorWithPastScheduledAtShouldThrowShowException() {
    // Arrange
    final var showId = new ShowId("S00000000000000000");
    final var pastScheduledAt = Instant.now().minus(1L, ChronoUnit.DAYS);
    final var movie = MovieFixture.newMovie("M00000000000000000");
    final var hall = HallFixture.newHall("H00000000000000000");

    // Act & Assert
    final var exception = assertThrows(ShowException.class,
      () -> new Show(showId, pastScheduledAt, movie, hall));

    assertEquals(ShowException.PAST_SCHEDULE_PROBLEM, exception.getProblem());
  }

  @Test
  void equalsShouldBeBasedOnShowId() {
    // Arrange
    final var show1 = ShowFixture.newShow("S00000000000000000");
    final var show2 = ShowFixture.newShow("S00000000000000000");
    final var show3 = ShowFixture.newShow("S00000000000000001");

    // Act & Assert
    assertEquals(show1, show2);
    assertNotEquals(show1, show3);
  }

  @Test
  void hashCodeShouldBeBasedOnShowId() {
    // Arrange
    final var show1 = ShowFixture.newShow("S00000000000000000");
    final var show2 = ShowFixture.newShow("S00000000000000000");

    // Act & Assert
    assertEquals(show1.hashCode(), show2.hashCode());
  }
}
