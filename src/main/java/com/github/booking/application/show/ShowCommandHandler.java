package com.github.booking.application.show;

import com.github.booking.application.show.command.ScheduleShowCommand;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ShowCommandHandler {

  void scheduleShow(ScheduleShowCommand command);
}
