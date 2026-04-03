package com.github.scheduling.infrastructure.web.show;

import com.github.scheduling.application.show.query.ShowDetailView;
import com.github.scheduling.application.show.query.ShowSummaryView;
import com.github.scheduling.infrastructure.web.representation.ListShowsResponse;
import com.github.scheduling.infrastructure.web.representation.ShowDetailResponse;
import com.github.scheduling.infrastructure.web.representation.ShowSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowMapper {

  public ShowDetailResponse toShowDetailResponse(final ShowDetailView view) {
    return new ShowDetailResponse()
      .showId(view.showId())
      .scheduledAt(view.scheduledAt())
      .movieId(view.movieId())
      .movieTitle(view.movieTitle())
      .movieRuntimeMinutes(view.movieRuntimeMinutes())
      .hallId(view.hallId())
      .hallName(view.hallName())
      .hallSeatCount(view.hallSeatCount());
  }

  public ListShowsResponse toListShowsResponse(final List<ShowSummaryView> views) {
    return new ListShowsResponse()
      .shows(views.stream()
        .map(v -> new ShowSummaryResponse()
          .showId(v.showId())
          .scheduledAt(v.scheduledAt())
          .movieTitle(v.movieTitle())
          .hallName(v.hallName()))
        .toList());
  }
}
