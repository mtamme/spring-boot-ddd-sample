package com.github.scheduling.infrastructure.service.hall;

import com.github.scheduling.domain.hall.Hall;
import com.github.scheduling.domain.hall.HallId;
import com.github.scheduling.domain.hall.HallService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockHallService implements HallService {

  private static final List<Hall> HALLS = List.of(
    new Hall(new HallId("H00000000000000001"), "Main Hall", 300),
    new Hall(new HallId("H00000000000000002"), "Hall A", 150),
    new Hall(new HallId("H00000000000000003"), "Hall B", 150),
    new Hall(new HallId("H00000000000000004"), "VIP Lounge", 50),
    new Hall(new HallId("H00000000000000005"), "IMAX Theater", 400));

  @Override
  public Hall hallFrom(final HallId hallId) {
    return HALLS.stream()
      .filter(hall -> hall.hallId().equals(hallId))
      .findFirst()
      .orElseGet(() -> {
        final var name = "Hall %d".formatted(1 + Math.abs(hallId.hashCode()) % 10);

        return new Hall(hallId, name, 150);
      });
  }

  @Override
  public List<Hall> listHalls() {
    return HALLS;
  }
}
