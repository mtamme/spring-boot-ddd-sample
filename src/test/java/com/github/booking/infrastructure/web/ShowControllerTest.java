package com.github.booking.infrastructure.web;

import com.github.booking.application.show.ShowQueryHandler;
import com.github.booking.application.show.query.GetShowQuery;
import com.github.booking.application.show.query.ListSeatsQuery;
import com.github.booking.application.show.query.ListShowsQuery;
import com.github.booking.application.show.query.SearchShowsQuery;
import com.github.booking.application.show.query.SeatBookingView;
import com.github.booking.application.show.query.SeatView;
import com.github.booking.application.show.query.ShowDetailView;
import com.github.booking.application.show.query.ShowHallView;
import com.github.booking.application.show.query.ShowMovieView;
import com.github.booking.application.show.query.ShowSummaryView;
import com.github.booking.infrastructure.web.representation.GetShowResponse;
import com.github.booking.infrastructure.web.representation.ListSeatsResponse;
import com.github.booking.infrastructure.web.representation.ListShowsResponse;
import com.github.booking.infrastructure.web.representation.SearchShowsResponse;
import com.github.seedwork.infrastructure.web.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShowControllerTest extends ControllerTest {

  @MockitoBean
  private ShowQueryHandler showQueryHandler;

  @Test
  void getShowShouldReturnGetShowResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(GetShowQuery.class);

    when(showQueryHandler.getShow(queryCaptor.capture()))
      .thenReturn(new ShowDetailView(
        "S00000000000000000",
        Instant.EPOCH,
        new ShowMovieView("M00000000000000000", "TestTitle"),
        new ShowHallView("H00000000000000000", "TestName")));

    // Act
    final var entity = client().get()
      .uri("/shows/{show_id}", "S00000000000000000")
      .retrieve()
      .toEntity(GetShowResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("S00000000000000000", response.getShowId());
    assertEquals(Instant.EPOCH, response.getScheduledAt());
    final var movie = response.getMovie();

    assertEquals("M00000000000000000", movie.getMovieId());
    assertEquals("TestTitle", movie.getTitle());
    final var hall = response.getHall();

    assertEquals("H00000000000000000", hall.getHallId());
    assertEquals("TestName", hall.getName());
    final var query = queryCaptor.getValue();

    assertEquals("S00000000000000000", query.showId());
  }

  @Test
  void listSeatsShouldReturnListSeatsResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListSeatsQuery.class);

    when(showQueryHandler.listSeats(queryCaptor.capture()))
      .thenReturn(List.of(new SeatView(
        "A1",
        "RESERVED",
        new SeatBookingView(
          "B00000000000000000",
          "INITIATED"))));

    // Act
    final var entity = client().get()
      .uri("/shows/{show_id}/seats", "S00000000000000000")
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
    assertEquals("B00000000000000000", booking.getBookingId());
    assertEquals("INITIATED", booking.getStatus());
    final var query = queryCaptor.getValue();

    assertEquals("S00000000000000000", query.showId());
  }

  @Test
  void listShowsShouldReturnListShowsResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListShowsQuery.class);

    when(showQueryHandler.listShows(queryCaptor.capture()))
      .thenReturn(List.of(new ShowSummaryView(
        "S00000000000000000",
        Instant.EPOCH,
        new ShowMovieView("M00000000000000000", "TestTitle"),
        new ShowHallView("H00000000000000000", "TestName"))));

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

    assertEquals("S00000000000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
    final var movie = show.getMovie();

    assertEquals("M00000000000000000", movie.getMovieId());
    assertEquals("TestTitle", movie.getTitle());
    final var hall = show.getHall();

    assertEquals("H00000000000000000", hall.getHallId());
    assertEquals("TestName", hall.getName());
    final var query = queryCaptor.getValue();

    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }

  @Test
  void searchShowsShouldReturnSearchShowsResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(SearchShowsQuery.class);

    when(showQueryHandler.searchShows(queryCaptor.capture()))
      .thenReturn(List.of(new ShowSummaryView(
        "S00000000000000000",
        Instant.EPOCH,
        new ShowMovieView("M00000000000000000", "TestTitle"),
        new ShowHallView("H00000000000000000", "TestName"))));

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

    assertEquals("S00000000000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
    final var movie = show.getMovie();

    assertEquals("M00000000000000000", movie.getMovieId());
    assertEquals("TestTitle", movie.getTitle());
    final var hall = show.getHall();

    assertEquals("H00000000000000000", hall.getHallId());
    assertEquals("TestName", hall.getName());
    final var query = queryCaptor.getValue();

    assertEquals("test title", query.query());
    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }
}
