package com.github.scheduling.domain.show;

import java.util.Optional;

public interface ShowRepository {

  ShowId nextShowId();

  Optional<Show> findByShowId(ShowId showId);

  void save(Show show);
}
