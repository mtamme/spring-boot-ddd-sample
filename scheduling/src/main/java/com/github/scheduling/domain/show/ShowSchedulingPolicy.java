package com.github.scheduling.domain.show;

import com.github.scheduling.domain.hall.HallId;

import java.time.Instant;

public interface ShowSchedulingPolicy {

  void ensureNoOverlap(HallId hallId, Instant start, Instant end);
}
