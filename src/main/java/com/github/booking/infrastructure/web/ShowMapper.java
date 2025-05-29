package com.github.booking.infrastructure.web;

import com.github.booking.application.show.query.SeatView;
import com.github.booking.application.show.query.ShowDetailView;
import com.github.booking.application.show.query.ShowSummaryView;
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

  private List<ShowSummary> toShowSummaries(final List<ShowSummaryView> shows) {
    return shows.stream()
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
  }

  private List<Seat> toSeats(final List<SeatView> seats) {
    return seats.stream()
      .map(s -> new Seat()
        .seatNumber(s.seatNumber())
        .status(s.status())
        .booking(Optional.ofNullable(s.booking())
          .map(sb -> new SeatBooking()
            .bookingId(sb.bookingId())
            .status(sb.status()))
          .orElse(null)))
      .toList();
  }

  public GetShowResponse toGetShowResponse(final ShowDetailView show) {
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

  public ListShowsResponse toListShowsResponse(final List<ShowSummaryView> shows) {
    return new ListShowsResponse()
      .shows(toShowSummaries(shows));
  }

  public SearchShowsResponse toSearchShowsResponse(final List<ShowSummaryView> shows) {
    return new SearchShowsResponse()
      .shows(toShowSummaries(shows));
  }

  public ListSeatsResponse toListSeatsResponse(final List<SeatView> seats) {
    return new ListSeatsResponse()
      .seats(toSeats(seats));
  }
}
