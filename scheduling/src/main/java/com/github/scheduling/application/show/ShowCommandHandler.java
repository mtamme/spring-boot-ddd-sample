package com.github.scheduling.application.show;

import com.github.scheduling.application.show.command.ScheduleShowCommand;
import com.github.scheduling.application.show.command.ScheduleShowResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ShowCommandHandler {

  ScheduleShowResult scheduleShow(ScheduleShowCommand command);
}
