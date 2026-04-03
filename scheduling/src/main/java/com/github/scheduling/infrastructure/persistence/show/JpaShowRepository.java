package com.github.scheduling.infrastructure.persistence.show;

import com.github.scheduling.application.show.query.ShowDetailView;
import com.github.scheduling.application.show.query.ShowSummaryView;
import com.github.scheduling.domain.hall.HallId;
import com.github.scheduling.domain.show.Show;
import com.github.scheduling.domain.show.ShowId;
import com.github.scheduling.domain.show.ShowRepository;
import com.github.seedwork.core.util.Randoms;
import com.github.seedwork.infrastructure.persistence.repository.JpaAggregateRootSupport;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JpaShowRepository extends JpaAggregateRootSupport, Repository<Show, Long>, ShowRepository {

  @Override
  default ShowId nextShowId() {
    return new ShowId("S0%016X".formatted(Randoms.nextLong()));
  }

  @Override
  default void save(final Show show) {
    saveAndPublishEvents(show.showId().value(), show);
  }

  @Override
  long countOverlappingShows(@Param("hallId") HallId hallId,
                             @Param("startTime") Instant start,
                             @Param("endTime") Instant end);

  @NativeQuery(name = "ShowDetailView.getShow")
  Optional<ShowDetailView> getShow(@Param("show_id") String showId);

  @NativeQuery(name = "ShowSummaryView.listShows")
  List<ShowSummaryView> listShows(@Param("offset") long offset, @Param("limit") int limit);
}
