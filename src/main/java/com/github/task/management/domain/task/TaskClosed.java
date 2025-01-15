package com.github.task.management.domain.task;

import com.github.task.management.domain.project.ProjectId;

public class TaskClosed extends TaskEvent {

  public TaskClosed(final ProjectId projectId, final TaskId taskId) {
    super(projectId, taskId);
  }
}
