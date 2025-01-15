package com.github.task.management.application.task;

import com.github.task.management.application.task.command.AssignTaskCommand;
import com.github.task.management.application.task.command.CloseTaskCommand;
import com.github.task.management.application.task.command.OpenTaskCommand;
import com.github.task.management.application.task.command.StartTaskCommand;
import com.github.task.management.application.task.command.UnassignTaskCommand;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TaskCommandHandler {

  String openTask(OpenTaskCommand command);

  void assignTask(AssignTaskCommand command);

  void unassignTask(UnassignTaskCommand command);

  void startTask(StartTaskCommand command);

  void closeTask(CloseTaskCommand command);
}
