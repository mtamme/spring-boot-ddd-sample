package com.github.booking.infrastructure.web;

import com.github.booking.application.show.ShowQueryHandler;
import com.github.booking.infrastructure.web.representation.GetShowResponse;
import com.github.booking.infrastructure.web.representation.ListSeatsResponse;
import com.github.booking.infrastructure.web.representation.ListShowsResponse;
import com.github.booking.infrastructure.web.representation.SearchShowsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ShowController implements ShowOperations {

  private final ShowQueryHandler showQueryHandler;
  private final ShowMapper showMapper;

  public ShowController(final ShowQueryHandler showQueryHandler, final ShowMapper showMapper) {
    this.showQueryHandler = Objects.requireNonNull(showQueryHandler);
    this.showMapper = Objects.requireNonNull(showMapper);
  }

  @Override
  public ResponseEntity<GetShowResponse> getShow(final String showId) {
    final var show = showQueryHandler.getShow(showId);
    final var body = showMapper.toGetShowResponse(show);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListShowsResponse> listShows(final Long offset, final Integer limit) {
    final var shows = showQueryHandler.listShows(offset, limit);
    final var body = showMapper.toListShowsResponse(shows);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<SearchShowsResponse> searchShows(final String query, final Long offset, final Integer limit) {
    final var shows = showQueryHandler.searchShows(query, offset, limit);
    final var body = showMapper.toSearchShowsResponse(shows);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListSeatsResponse> listSeats(final String showId) {
    final var seats = showQueryHandler.listSeats(showId);
    final var body = showMapper.toListSeatsResponse(seats);

    return ResponseEntity.ok(body);
  }
}
