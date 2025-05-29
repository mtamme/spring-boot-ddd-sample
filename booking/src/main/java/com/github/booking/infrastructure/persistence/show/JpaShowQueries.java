package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.query.GetShow;
import com.github.booking.application.show.query.ListSeats;
import com.github.booking.application.show.query.ListShows;
import com.github.booking.application.show.query.SearchShows;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaShowQueries {

  @NativeQuery(name = "Show.getShow")
  Optional<GetShow.Show> getShow(@Param("show_id") String showId);

  @NativeQuery(name = "Show.listShows")
  List<ListShows.Show> listShows(@Param("offset") long offset,
                                 @Param("limit") int limit);

  @NativeQuery(name = "Show.searchShows")
  List<SearchShows.Show> searchShows(@Param("filter_pattern") String filterPattern,
                                     @Param("sort_pattern") String sortPattern,
                                     @Param("offset") long offset,
                                     @Param("limit") int limit);

  @NativeQuery(name = "Show.listSeats")
  List<ListSeats.Seat> listSeats(@Param("show_id") String showId);
}
