package com.github.booking.api;

import com.github.booking.api.representation.GetShowResponse;
import com.github.booking.api.representation.ListShowsResponse;
import com.github.booking.api.representation.SearchShowsResponse;
import com.github.booking.api.representation.Seat;
import com.github.booking.api.representation.SeatBooking;
import com.github.booking.api.representation.ShowHall;
import com.github.booking.api.representation.ShowMovie;
import com.github.booking.api.representation.ShowSummary;
import com.github.booking.application.show.ShowQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
public class ShowController implements ShowOperations {

  private final ShowQueryHandler showQueryHandler;

  public ShowController(final ShowQueryHandler showQueryHandler) {
    this.showQueryHandler = Objects.requireNonNull(showQueryHandler);
  }

  @Override
  public ResponseEntity<GetShowResponse> getShow(final String showId) {
    final var show = showQueryHandler.getShow(showId);
    final var response = new GetShowResponse()
      .showId(show.showId())
      .scheduledAt(show.scheduledAt())
      .movie(Optional.ofNullable(show.movie())
        .map(sm -> new ShowMovie()
          .movieId(sm.movieId())
          .title(sm.title()))
        .orElse(null))
      .hall(Optional.ofNullable(show.hall())
        .map(sh -> new ShowHall()
          .hallId(sh.hallId())
          .name(sh.name()))
        .orElse(null))
      .seats(show.seats()
        .stream()
        .map(ss -> new Seat()
          .seatNumber(ss.seatNumber())
          .status(ss.status())
          .booking(Optional.ofNullable(ss.booking())
            .map(ssb -> new SeatBooking()
              .bookingId(ssb.bookingId())
              .status(ssb.status()))
            .orElse(null)))
        .toList());

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ListShowsResponse> listShows(final Long offset, final Integer limit) {
    final var shows = showQueryHandler.listShows(offset, limit)
      .stream()
      .map(s -> new ShowSummary()
        .showId(s.showId())
        .scheduledAt(s.scheduledAt())
        .movie(Optional.ofNullable(s.movie())
          .map(sm -> new ShowMovie()
            .movieId(sm.movieId())
            .title(sm.title()))
          .orElse(null))
        .hall(Optional.ofNullable(s.hall())
          .map(sh -> new ShowHall()
            .hallId(sh.hallId())
            .name(sh.name()))
          .orElse(null)))
      .toList();
    final var response = new ListShowsResponse()
      .shows(shows);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<SearchShowsResponse> searchShows(final String query, final Long offset, final Integer limit) {
    final var shows = showQueryHandler.searchShows(query, offset, limit)
      .stream()
      .map(s -> new ShowSummary()
        .showId(s.showId())
        .scheduledAt(s.scheduledAt())
        .movie(Optional.ofNullable(s.movie())
          .map(sm -> new ShowMovie()
            .movieId(sm.movieId())
            .title(sm.title()))
          .orElse(null))
        .hall(Optional.ofNullable(s.hall())
          .map(sh -> new ShowHall()
            .hallId(sh.hallId())
            .name(sh.name()))
          .orElse(null)))
      .toList();
    final var response = new SearchShowsResponse()
      .shows(shows);

    return ResponseEntity.ok(response);
  }
}
