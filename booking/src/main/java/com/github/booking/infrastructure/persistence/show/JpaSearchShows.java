package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.SearchShows;
import com.github.seedwork.infrastructure.persistence.SqlLike;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
class JpaSearchShows implements SearchShows {

  private final JpaShowQueries queries;

  public JpaSearchShows(final JpaShowQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public List<Show> handle(final Query query) {
    final var sqlLike = SqlLike.of(query.term());

    return queries.searchShows(sqlLike.containsPattern(), sqlLike.startsWithPattern(), query.offset(), query.limit());
  }
}
