package com.github.task.management.domain.task;

import com.github.task.management.domain.project.ProjectId;

public class TaskOpened extends TaskEvent {

  public TaskOpened(final ProjectId projectId, final TaskId taskId) {
    super(projectId, taskId);
  }
}
