package com.github.booking.infrastructure.service.hall;

import com.github.booking.domain.hall.Hall;
import com.github.booking.domain.hall.HallId;
import com.github.booking.domain.hall.HallService;
import com.github.booking.domain.hall.SeatLayout;
import org.springframework.stereotype.Service;

@Service
public class MockHallService implements HallService {

  @Override
  public Hall hallFrom(final HallId hallId) {
    final var name = "Hall %d".formatted(1 + Math.abs(hallId.hashCode()) % 10);

    return new Hall(hallId, name, new SeatLayout(10, 15));
  }
}
