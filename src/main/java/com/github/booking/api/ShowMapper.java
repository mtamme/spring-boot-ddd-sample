package com.github.booking.api;

import com.github.booking.api.representation.GetShowResponse;
import com.github.booking.api.representation.ListSeatsResponse;
import com.github.booking.api.representation.ListShowsResponse;
import com.github.booking.api.representation.SearchShowsResponse;
import com.github.booking.api.representation.Seat;
import com.github.booking.api.representation.ShowSummary;
import com.github.booking.application.show.view.SeatView;
import com.github.booking.application.show.view.ShowDetailView;
import com.github.booking.application.show.view.ShowSummaryView;

import java.util.List;

public interface ShowMapper {

  GetShowResponse toGetShowResponse(ShowDetailView show);

  default ListShowsResponse toListShowsResponse(final List<ShowSummaryView> shows) {
    return new ListShowsResponse()
      .shows(toShowSummaries(shows));
  }

  default SearchShowsResponse toSearchShowsResponse(final List<ShowSummaryView> shows) {
    return new SearchShowsResponse()
      .shows(toShowSummaries(shows));
  }

  List<ShowSummary> toShowSummaries(List<ShowSummaryView> shows);

  default ListSeatsResponse toListSeatsResponse(final List<SeatView> seats) {
    return new ListSeatsResponse()
      .seats(toSeats(seats));
  }

  List<Seat> toSeats(List<SeatView> seats);
}
