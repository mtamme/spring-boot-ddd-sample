package com.github.scheduling.infrastructure.persistence.show;

import com.github.scheduling.application.show.ShowQueryHandler;
import com.github.scheduling.application.show.query.GetShowQuery;
import com.github.scheduling.application.show.query.ListShowsQuery;
import com.github.scheduling.application.show.query.ShowDetailView;
import com.github.scheduling.application.show.query.ShowSummaryView;
import com.github.scheduling.domain.show.ShowException;
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
}
