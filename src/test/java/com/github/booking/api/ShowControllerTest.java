package com.github.booking.api;

import com.github.booking.api.representation.GetShowResponse;
import com.github.booking.api.representation.ListSeatsResponse;
import com.github.booking.api.representation.ListShowsResponse;
import com.github.booking.api.representation.SearchShowsResponse;
import com.github.booking.application.show.ShowQueryHandler;
import com.github.booking.application.show.view.SeatBookingView;
import com.github.booking.application.show.view.SeatView;
import com.github.booking.application.show.view.ShowDetailView;
import com.github.booking.application.show.view.ShowHallView;
import com.github.booking.application.show.view.ShowMovieView;
import com.github.booking.application.show.view.ShowSummaryView;
import com.github.seedwork.api.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShowControllerTest extends ControllerTest {

  @MockitoBean
  private ShowQueryHandler showQueryHandler;

  @Test
  void getShowShouldReturnGetShowResponse() {
    // Arrange
    Mockito.when(showQueryHandler.getShow("S0000000000"))
      .thenReturn(new ShowDetailView(
        "S0000000000",
        Instant.EPOCH,
        new ShowMovieView("M0000000000", "TestTitle"),
        new ShowHallView("H0000000000", "TestName")));

    // Act
    final var entity = client().get()
      .uri("/shows/{show_id}", "S0000000000")
      .retrieve()
      .toEntity(GetShowResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("S0000000000", response.getShowId());
    assertEquals(Instant.EPOCH, response.getScheduledAt());
    final var movie = response.getMovie();

    assertEquals("M0000000000", movie.getMovieId());
    assertEquals("TestTitle", movie.getTitle());
    final var hall = response.getHall();

    assertEquals("H0000000000", hall.getHallId());
    assertEquals("TestName", hall.getName());
  }

  @Test
  void listSeatsShouldReturnListSeatsResponse() {
    // Arrange
    Mockito.when(showQueryHandler.listSeats("S0000000000"))
      .thenReturn(List.of(new SeatView(
        "A1",
        "RESERVED",
        new SeatBookingView(
          "B0000000000",
          "INITIATED"))));

    // Act
    final var entity = client().get()
      .uri("/shows/{show_id}/seats", "S0000000000")
      .retrieve()
      .toEntity(ListSeatsResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var seats = response.getSeats();

    assertEquals(1, seats.size());
    final var seat = seats.getFirst();

    assertEquals("A1", seat.getSeatNumber());
    assertEquals("RESERVED", seat.getStatus());
    final var booking = seat.getBooking();

    assertNotNull(booking);
    assertEquals("B0000000000", booking.getBookingId());
    assertEquals("INITIATED", booking.getStatus());
  }

  @Test
  void listShowsShouldReturnListShowsResponse() {
    // Arrange
    Mockito.when(showQueryHandler.listShows(0, 10))
      .thenReturn(List.of(new ShowSummaryView(
        "S0000000000",
        Instant.EPOCH,
        new ShowMovieView("M0000000000", "TestTitle"),
        new ShowHallView("H0000000000", "TestName"))));

    // Act
    final var entity = client().get()
      .uri("/shows")
      .retrieve()
      .toEntity(ListShowsResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var shows = response.getShows();

    assertEquals(1, shows.size());
    final var show = shows.getFirst();

    assertEquals("S0000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
    final var movie = show.getMovie();

    assertEquals("M0000000000", movie.getMovieId());
    assertEquals("TestTitle", movie.getTitle());
    final var hall = show.getHall();

    assertEquals("H0000000000", hall.getHallId());
    assertEquals("TestName", hall.getName());
  }

  @Test
  void searchShowsShouldReturnSearchShowsResponse() {
    // Arrange
    Mockito.when(showQueryHandler.searchShows("test title", 0, 10))
      .thenReturn(List.of(new ShowSummaryView(
        "S0000000000",
        Instant.EPOCH,
        new ShowMovieView("M0000000000", "TestTitle"),
        new ShowHallView("H0000000000", "TestName"))));

    // Act
    final var entity = client().get()
      .uri("/search/shows?q={query}", "test title")
      .retrieve()
      .toEntity(SearchShowsResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var shows = response.getShows();

    assertEquals(1, shows.size());
    final var show = shows.getFirst();

    assertEquals("S0000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
    final var movie = show.getMovie();

    assertEquals("M0000000000", movie.getMovieId());
    assertEquals("TestTitle", movie.getTitle());
    final var hall = show.getHall();

    assertEquals("H0000000000", hall.getHallId());
    assertEquals("TestName", hall.getName());
  }
}
