package com.github.scheduling.application.hall;

import com.github.scheduling.application.hall.query.HallView;
import com.github.scheduling.application.hall.query.ListHallsQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface HallQueryHandler {
  List<HallView> listHalls(ListHallsQuery query);
}
