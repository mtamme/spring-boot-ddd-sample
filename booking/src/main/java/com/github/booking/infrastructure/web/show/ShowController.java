package com.github.booking.infrastructure.web.show;

import com.github.booking.application.show.query.GetShow;
import com.github.booking.application.show.query.ListSeats;
import com.github.booking.application.show.query.ListShows;
import com.github.booking.application.show.query.SearchShows;
import com.github.booking.infrastructure.web.ShowOperations;
import com.github.booking.infrastructure.web.representation.GetShowResponse;
import com.github.booking.infrastructure.web.representation.ListSeatsResponse;
import com.github.booking.infrastructure.web.representation.ListShowsResponse;
import com.github.booking.infrastructure.web.representation.SearchShowsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ShowController implements ShowOperations {

  private final GetShow getShow;
  private final ListShows listShows;
  private final SearchShows searchShows;
  private final ListSeats listSeats;
  private final ShowMapper showMapper;

  public ShowController(final GetShow getShow,
                        final ListShows listShows,
                        final SearchShows searchShows,
                        final ListSeats listSeats,
                        final ShowMapper showMapper) {
    this.getShow = Objects.requireNonNull(getShow);
    this.listShows = Objects.requireNonNull(listShows);
    this.searchShows = Objects.requireNonNull(searchShows);
    this.listSeats = Objects.requireNonNull(listSeats);
    this.showMapper = Objects.requireNonNull(showMapper);
  }

  @Override
  public ResponseEntity<GetShowResponse> getShow(final String showId) {
    final var show = getShow.handle(new GetShow.Query(showId));
    final var body = showMapper.toGetShowResponse(show);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListShowsResponse> listShows(final Long offset, final Integer limit) {
    final var shows = listShows.handle(new ListShows.Query(offset, limit));
    final var body = showMapper.toListShowsResponse(shows);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<SearchShowsResponse> searchShows(final String term, final Long offset, final Integer limit) {
    final var shows = searchShows.handle(new SearchShows.Query(term, offset, limit));
    final var body = showMapper.toSearchShowsResponse(shows);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListSeatsResponse> listSeats(final String showId) {
    final var seats = listSeats.handle(new ListSeats.Query(showId));
    final var body = showMapper.toListSeatsResponse(seats);

    return ResponseEntity.ok(body);
  }
}
