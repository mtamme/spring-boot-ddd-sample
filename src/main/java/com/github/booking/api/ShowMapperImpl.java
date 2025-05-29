package com.github.booking.api;

import com.github.booking.api.representation.GetShowResponse;
import com.github.booking.api.representation.Seat;
import com.github.booking.api.representation.SeatBooking;
import com.github.booking.api.representation.ShowHall;
import com.github.booking.api.representation.ShowMovie;
import com.github.booking.api.representation.ShowSummary;
import com.github.booking.application.show.view.SeatView;
import com.github.booking.application.show.view.ShowDetailView;
import com.github.booking.application.show.view.ShowSummaryView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ShowMapperImpl implements ShowMapper {

  @Override
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

  @Override
  public List<ShowSummary> toShowSummaries(final List<ShowSummaryView> shows) {
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

  @Override
  public List<Seat> toSeats(final List<SeatView> seats) {
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
}
