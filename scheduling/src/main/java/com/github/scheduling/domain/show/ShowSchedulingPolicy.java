package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.HallId;
import com.github.seedwork.domain.Contract;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ShowSchedulingPolicy {

  private final ShowRepository showRepository;

  public ShowSchedulingPolicy(final ShowRepository showRepository) {
    this.showRepository = showRepository;
  }

  public void ensureNoOverlap(final HallId hallId, final Instant start, final Instant end) {
    Contract.require(hallId != null);
    Contract.require(start != null);
    Contract.require(end != null);

    final var count = showRepository.countOverlappingShows(hallId, start, end);

    Contract.check(count == 0, ShowException::overlap);
  }
}
