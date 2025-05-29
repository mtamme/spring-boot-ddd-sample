package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.ListShows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
class JpaListShows implements ListShows {

  private final JpaShowQueries queries;

  public JpaListShows(final JpaShowQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public List<Show> handle(final Query query) {
    return queries.listShows(query.offset(), query.limit());
  }
}
