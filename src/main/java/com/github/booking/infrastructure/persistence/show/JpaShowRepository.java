package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.SeatView;
import com.github.booking.application.show.query.ShowDetailView;
import com.github.booking.application.show.query.ShowSummaryView;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowRepository;
import com.github.seedwork.infrastructure.persistence.JpaAggregateSaver;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaShowRepository extends JpaAggregateSaver<Show>, Repository<Show, Long>, ShowRepository {

  @NativeQuery(name = "ShowDetailView.getShow")
  Optional<ShowDetailView> getShow(@Param("show_id") String showId);

  @NativeQuery(name = "ShowSummaryView.listShows")
  List<ShowSummaryView> listShows(@Param("offset") long offset,
                                  @Param("limit") int limit);

  @NativeQuery(name = "ShowSummaryView.searchShows")
  List<ShowSummaryView> searchShows(@Param("filter_pattern") String filterPattern,
                                    @Param("sort_pattern") String sortPattern,
                                    @Param("offset") long offset,
                                    @Param("limit") int limit);

  @NativeQuery(name = "SeatView.listSeats")
  List<SeatView> listSeats(@Param("show_id") String showId);

  @Override
  default void save(final Show show) {
    saveAndPublishEvents(show.showId().value(), show);
  }
}
