package com.github.scheduling.application.show;

import com.github.scheduling.application.show.query.HallView;
import com.github.scheduling.application.show.query.ListHallsQuery;
import com.github.scheduling.domain.hall.HallService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class HallQueryHandlerImpl implements HallQueryHandler {

  private final HallService hallService;

  HallQueryHandlerImpl(final HallService hallService) {
    this.hallService = hallService;
  }

  @Override
  public List<HallView> listHalls(final ListHallsQuery query) {
    return hallService.listHalls().stream()
      .map(hall -> new HallView(hall.hallId().value(), hall.name(), hall.seatCount()))
      .toList();
  }
}
