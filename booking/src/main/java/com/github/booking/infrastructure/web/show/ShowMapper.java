package com.github.booking.infrastructure.web.show;

import com.github.booking.application.show.query.GetShow;
import com.github.booking.application.show.query.ListSeats;
import com.github.booking.application.show.query.ListShows;
import com.github.booking.application.show.query.SearchShows;
import com.github.booking.infrastructure.web.representation.GetShowResponse;
import com.github.booking.infrastructure.web.representation.ListSeatsResponse;
import com.github.booking.infrastructure.web.representation.ListShowsResponse;
import com.github.booking.infrastructure.web.representation.SearchShowsResponse;
import com.github.booking.infrastructure.web.representation.Seat;
import com.github.booking.infrastructure.web.representation.SeatBooking;
import com.github.booking.infrastructure.web.representation.ShowHall;
import com.github.booking.infrastructure.web.representation.ShowMovie;
import com.github.booking.infrastructure.web.representation.ShowSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ShowMapper {

  public GetShowResponse toGetShowResponse(final GetShow.Show show) {
    return new GetShowResponse()
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
        .orElse(null));
  }

  public ListShowsResponse toListShowsResponse(final List<ListShows.Show> shows) {
    return new ListShowsResponse()
      .shows(shows.stream()
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
        .toList());
  }

  public SearchShowsResponse toSearchShowsResponse(final List<SearchShows.Show> shows) {
    return new SearchShowsResponse()
      .shows(shows.stream()
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
        .toList());
  }

  public ListSeatsResponse toListSeatsResponse(final List<ListSeats.Seat> seats) {
    return new ListSeatsResponse()
      .seats(seats.stream()
        .map(s -> new Seat()
          .seatNumber(s.seatNumber())
          .status(s.status())
          .booking(Optional.ofNullable(s.booking())
            .map(sb -> new SeatBooking()
              .bookingId(sb.bookingId())
              .status(sb.status()))
            .orElse(null)))
        .toList());
  }
}
