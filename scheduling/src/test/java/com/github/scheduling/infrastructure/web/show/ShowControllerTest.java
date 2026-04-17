package com.github.scheduling.infrastructure.web.show;

import com.github.scheduling.application.show.ShowQueryHandler;
import com.github.scheduling.application.show.query.GetShowQuery;
import com.github.scheduling.application.show.query.ListShowsQuery;
import com.github.scheduling.application.show.query.ShowDetailView;
import com.github.scheduling.application.show.query.ShowSummaryView;
import com.github.scheduling.infrastructure.web.ControllerTest;
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
  private ShowQueryHandler showQueryHandler;

  @Test
  void getShowShouldReturnShowDetailResponse() throws Exception {
    final var queryCaptor = ArgumentCaptor.forClass(GetShowQuery.class);

    when(showQueryHandler.getShow(queryCaptor.capture()))
      .thenReturn(new ShowDetailView(
        "S00000000000000000",
        Instant.EPOCH,
        "M00000000000000000",
        "TestTitle",
        120,
        "H00000000000000000",
        "TestName",
        150));

    mockMvc().perform(get("/shows/{show_id}", "S00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.showId").value("S00000000000000000"))
      .andExpect(jsonPath("$.scheduledAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.movieId").value("M00000000000000000"))
      .andExpect(jsonPath("$.movieTitle").value("TestTitle"))
      .andExpect(jsonPath("$.movieRuntimeMinutes").value(120))
      .andExpect(jsonPath("$.hallId").value("H00000000000000000"))
      .andExpect(jsonPath("$.hallName").value("TestName"))
      .andExpect(jsonPath("$.hallSeatCount").value(150));

    assertEquals("S00000000000000000", queryCaptor.getValue().showId());
  }

  @Test
  void getShowWithUnknownIdShouldReturn404() throws Exception {
    when(showQueryHandler.getShow(any()))
      .thenThrow(com.github.scheduling.domain.show.ShowException.notFound());

    mockMvc().perform(get("/shows/{show_id}", "S0FFFFFFFFFFFFFFFF")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound());
  }

  @Test
  void listShowsShouldReturnListShowsResponse() throws Exception {
    final var queryCaptor = ArgumentCaptor.forClass(ListShowsQuery.class);

    when(showQueryHandler.listShows(queryCaptor.capture()))
      .thenReturn(List.of(new ShowSummaryView(
        "S00000000000000000",
        Instant.EPOCH,
        "TestTitle",
        "TestName")));

    mockMvc().perform(get("/shows")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.shows.length()").value(1))
      .andExpect(jsonPath("$.shows[0].showId").value("S00000000000000000"))
      .andExpect(jsonPath("$.shows[0].scheduledAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.shows[0].movieTitle").value("TestTitle"))
      .andExpect(jsonPath("$.shows[0].hallName").value("TestName"));

    assertEquals(0L, queryCaptor.getValue().offset());
    assertEquals(10, queryCaptor.getValue().limit());
  }
}
