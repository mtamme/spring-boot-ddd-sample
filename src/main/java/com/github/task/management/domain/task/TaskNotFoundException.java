package com.github.task.management.domain.task;

import com.github.seedwork.core.problem.NotFoundException;

public class TaskNotFoundException extends NotFoundException {

  public TaskNotFoundException() {
    super("Task not found");
  }
}
