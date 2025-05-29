package com.github.booking.domain.show;

import java.util.Optional;

public interface ShowRepository {

  Optional<Show> findByShowId(ShowId showId);

  void save(Show show);
}
