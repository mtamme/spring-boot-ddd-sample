package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.ShowQueryHandler;
import com.github.booking.application.show.query.GetShowQuery;
import com.github.booking.application.show.query.ListSeatsQuery;
import com.github.booking.application.show.query.ListShowsQuery;
import com.github.booking.application.show.query.SearchShowsQuery;
import com.github.booking.application.show.query.SeatView;
import com.github.booking.application.show.query.ShowDetailView;
import com.github.booking.application.show.query.ShowSummaryView;
import com.github.booking.domain.show.ShowException;
import com.github.seedwork.infrastructure.persistence.SqlSearch;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JpaShowQueryHandler implements ShowQueryHandler {

  private final JpaShowRepository repository;

  public JpaShowQueryHandler(final JpaShowRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public ShowDetailView getShow(final GetShowQuery query) {
    return repository.getShow(query.showId())
      .orElseThrow(ShowException::notFound);
  }

  @Override
  public List<ShowSummaryView> listShows(final ListShowsQuery query) {
    return repository.listShows(query.offset(), query.limit());
  }

  @Override
  public List<ShowSummaryView> searchShows(final SearchShowsQuery query) {
    final var sqlSearch = SqlSearch.of(query.query());

    return repository.searchShows(sqlSearch.containsPattern(), sqlSearch.startsWithPattern(), query.offset(), query.limit());
  }

  @Override
  public List<SeatView> listSeats(final ListSeatsQuery query) {
    return repository.listSeats(query.showId());
  }
}
