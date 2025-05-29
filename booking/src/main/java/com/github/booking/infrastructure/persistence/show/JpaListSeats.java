package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.ListSeats;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
class JpaListSeats implements ListSeats {

  private final JpaShowQueries queries;

  public JpaListSeats(final JpaShowQueries queries) {
    this.queries = Objects.requireNonNull(queries);
  }

  @Override
  public List<Seat> handle(final Query query) {
    return queries.listSeats(query.showId());
  }
}
