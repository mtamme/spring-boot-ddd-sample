package com.github.booking.application.show;

import com.github.booking.application.show.view.SeatView;
import com.github.booking.application.show.view.ShowDetailView;
import com.github.booking.application.show.view.ShowSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ShowQueryHandler {

  ShowDetailView getShow(String showId);

  List<ShowSummaryView> listShows(long offset, int limit);

  List<ShowSummaryView> searchShows(String query, long offset, int limit);

  List<SeatView> listSeats(String showId);
}
