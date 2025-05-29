package com.github.booking.application.show.command;

import com.github.seedwork.application.CommandHandler;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Transactional
public interface ScheduleShow extends CommandHandler<ScheduleShow.Command> {

  record Command(String showId,
                 Instant scheduledAt,
                 String movieId,
                 String hallId) {
  }
}
