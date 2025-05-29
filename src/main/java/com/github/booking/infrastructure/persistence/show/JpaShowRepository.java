package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.view.SeatView;
import com.github.booking.application.show.view.ShowDetailView;
import com.github.booking.application.show.view.ShowSummaryView;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowRepository;
import com.github.seedwork.infrastructure.persistence.JpaAggregateSaver;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaShowRepository extends JpaAggregateSaver<Show>, Repository<Show, Long>, ShowRepository {

  @NativeQuery(name = "ShowDetailView.find")
  Optional<ShowDetailView> find(@Param("show_id") String showId);

  @NativeQuery(name = "ShowSummaryView.findAll")
  List<ShowSummaryView> findAll(@Param("offset") long offset,
                                @Param("limit") int limit);

  @NativeQuery(name = "ShowSummaryView.findAllByPattern")
  List<ShowSummaryView> findAllByPattern(@Param("filter_pattern") String filterPattern,
                                         @Param("sort_pattern") String sortPattern,
                                         @Param("offset") long offset,
                                         @Param("limit") int limit);

  @NativeQuery(name = "SeatView.findAll")
  List<SeatView> findAllSeats(@Param("show_id") String showId);

  @Override
  default void save(final Show show) {
    save(show.showId().value(), show);
  }
}
