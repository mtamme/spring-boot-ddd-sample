package com.github.scheduling.application.show;

import com.github.scheduling.application.show.query.GetShowQuery;
import com.github.scheduling.application.show.query.ListShowsQuery;
import com.github.scheduling.application.show.query.ShowDetailView;
import com.github.scheduling.application.show.query.ShowSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ShowQueryHandler {
  ShowDetailView getShow(GetShowQuery query);

  List<ShowSummaryView> listShows(ListShowsQuery query);
}
