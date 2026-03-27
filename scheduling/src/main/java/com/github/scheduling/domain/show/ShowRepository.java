package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.HallId;

import java.time.Instant;
import java.util.Optional;

public interface ShowRepository {

  ShowId nextShowId();

  Optional<Show> findByShowId(ShowId showId);

  void save(Show show);

  long countOverlappingShows(HallId hallId, Instant start, Instant end);
}
