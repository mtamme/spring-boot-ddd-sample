package com.github.booking.application.show;

import com.github.booking.application.show.query.GetShowQuery;
import com.github.booking.application.show.query.ListSeatsQuery;
import com.github.booking.application.show.query.ListShowsQuery;
import com.github.booking.application.show.query.SearchShowsQuery;
import com.github.booking.application.show.query.SeatView;
import com.github.booking.application.show.query.ShowDetailView;
import com.github.booking.application.show.query.ShowSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ShowQueryHandler {

  ShowDetailView getShow(GetShowQuery query);

  List<ShowSummaryView> listShows(ListShowsQuery query);

  List<ShowSummaryView> searchShows(SearchShowsQuery query);

  List<SeatView> listSeats(ListSeatsQuery query);
}
