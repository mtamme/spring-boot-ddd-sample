package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.GetShow;
import com.github.booking.domain.show.ShowException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class JpaGetShow implements GetShow {

  private final JpaShowQueries queries;

  public JpaGetShow(final JpaShowQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public Show handle(final Query query) {
    return queries.getShow(query.showId())
      .orElseThrow(ShowException::notFound);
  }
}
