package com.github.scheduling.infrastructure.persistence.show;

import com.github.scheduling.domain.hall.HallId;
import com.github.scheduling.domain.show.ShowException;
import com.github.scheduling.domain.show.ShowSchedulingPolicy;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JpaShowSchedulingPolicy implements ShowSchedulingPolicy {

  private final EntityManager entityManager;

  public JpaShowSchedulingPolicy(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void ensureNoOverlap(final HallId hallId, final Instant start, final Instant end) {
    final var query = entityManager.createNativeQuery("""
      SELECT COUNT(*)
      FROM show s
      WHERE s.hall_id = :hallId
        AND s.scheduled_at < :endTime
        AND DATEADD(MINUTE, s.movie_runtime_minutes, s.scheduled_at) > :startTime
      """, Long.class);

    query.setParameter("hallId", hallId.value());
    query.setParameter("startTime", start);
    query.setParameter("endTime", end);

    final var count = (Long) query.getSingleResult();

    if (count > 0) {
      throw ShowException.overlap();
    }
  }
}
