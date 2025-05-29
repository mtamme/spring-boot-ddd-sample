package com.github.booking.infrastructure.web.show;

import com.github.booking.application.show.query.GetShow;
import com.github.booking.application.show.query.ListSeats;
import com.github.booking.application.show.query.ListShows;
import com.github.booking.application.show.query.SearchShows;
import com.github.booking.infrastructure.web.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShowControllerTest extends ControllerTest {

  @MockitoBean
  private GetShow getShow;
  @MockitoBean
  private ListShows listShows;
  @MockitoBean
  private SearchShows searchShows;
  @MockitoBean
  private ListSeats listSeats;

  @Test
  void getShowShouldReturnGetShowResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(GetShow.Query.class);

    when(getShow.handle(queryCaptor.capture()))
      .thenReturn(new GetShow.Show(
        "S00000000000000000",
        Instant.EPOCH,
        new GetShow.Movie("M00000000000000000", "TestTitle"),
        new GetShow.Hall("H00000000000000000", "TestName")));

    // Act
    // Assert
    mockMvc().perform(get("/shows/{show_id}", "S00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.showId").value("S00000000000000000"))
      .andExpect(jsonPath("$.scheduledAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.movie.movieId").value("M00000000000000000"))
      .andExpect(jsonPath("$.movie.title").value("TestTitle"))
      .andExpect(jsonPath("$.hall.hallId").value("H00000000000000000"))
      .andExpect(jsonPath("$.hall.name").value("TestName"));
    final var query = queryCaptor.getValue();

    assertEquals("S00000000000000000", query.showId());
  }

  @Test
  void listSeatsShouldReturnListSeatsResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListSeats.Query.class);

    when(listSeats.handle(queryCaptor.capture()))
      .thenReturn(List.of(new ListSeats.Seat(
        "A1",
        "RESERVED",
        new ListSeats.Booking(
          "B00000000000000000",
          "INITIATED"))));

    // Act
    // Assert
    mockMvc().perform(get("/shows/{show_id}/seats", "S00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.seats.length()").value(1))
      .andExpect(jsonPath("$.seats[0].seatNumber").value("A1"))
      .andExpect(jsonPath("$.seats[0].status").value("RESERVED"))
      .andExpect(jsonPath("$.seats[0].booking.bookingId").value("B00000000000000000"))
      .andExpect(jsonPath("$.seats[0].booking.status").value("INITIATED"));
    final var query = queryCaptor.getValue();

    assertEquals("S00000000000000000", query.showId());
  }

  @Test
  void listShowsShouldReturnListShowsResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListShows.Query.class);

    when(listShows.handle(queryCaptor.capture()))
      .thenReturn(List.of(new ListShows.Show(
        "S00000000000000000",
        Instant.EPOCH,
        new ListShows.Movie("M00000000000000000", "TestTitle"),
        new ListShows.Hall("H00000000000000000", "TestName"))));

    // Act
    // Assert
    mockMvc().perform(get("/shows")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.shows.length()").value(1))
      .andExpect(jsonPath("$.shows[0].showId").value("S00000000000000000"))
      .andExpect(jsonPath("$.shows[0].scheduledAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.shows[0].movie.movieId").value("M00000000000000000"))
      .andExpect(jsonPath("$.shows[0].movie.title").value("TestTitle"))
      .andExpect(jsonPath("$.shows[0].hall.hallId").value("H00000000000000000"))
      .andExpect(jsonPath("$.shows[0].hall.name").value("TestName"));
    final var query = queryCaptor.getValue();

    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }

  @Test
  void searchShowsShouldReturnSearchShowsResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(SearchShows.Query.class);

    when(searchShows.handle(queryCaptor.capture()))
      .thenReturn(List.of(new SearchShows.Show(
        "S00000000000000000",
        Instant.EPOCH,
        new SearchShows.Movie("M00000000000000000", "TestTitle"),
        new SearchShows.Hall("H00000000000000000", "TestName"))));

    // Act
    // Assert
    mockMvc().perform(get("/search/shows?q={query}", "test title")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.shows.length()").value(1))
      .andExpect(jsonPath("$.shows[0].showId").value("S00000000000000000"))
      .andExpect(jsonPath("$.shows[0].scheduledAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.shows[0].movie.movieId").value("M00000000000000000"))
      .andExpect(jsonPath("$.shows[0].movie.title").value("TestTitle"))
      .andExpect(jsonPath("$.shows[0].hall.hallId").value("H00000000000000000"))
      .andExpect(jsonPath("$.shows[0].hall.name").value("TestName"));
    final var query = queryCaptor.getValue();

    assertEquals("test title", query.term());
    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }
}
