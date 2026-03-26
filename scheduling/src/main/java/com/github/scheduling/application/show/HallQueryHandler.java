package com.github.scheduling.application.show;

import com.github.scheduling.application.show.query.HallView;
import com.github.scheduling.application.show.query.ListHallsQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface HallQueryHandler {
  List<HallView> listHalls(ListHallsQuery query);
}
