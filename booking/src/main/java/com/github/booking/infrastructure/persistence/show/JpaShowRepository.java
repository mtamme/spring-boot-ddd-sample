package com.github.booking.infrastructure.persistence.show;

import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowRepository;
import com.github.seedwork.infrastructure.persistence.repository.JpaAggregateRootSupport;
import org.springframework.data.repository.Repository;

public interface JpaShowRepository extends JpaAggregateRootSupport, JpaShowQueries, ShowRepository, Repository<Show, Long> {

  @Override
  default void save(final Show show) {
    saveAndPublishEvents(show.showId().value(), show);
  }
}
