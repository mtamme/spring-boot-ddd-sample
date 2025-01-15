package com.github.task.management.domain.task;

import com.github.task.management.domain.project.ProjectId;

public class TaskStarted extends TaskEvent {

  public TaskStarted(final ProjectId projectId, final TaskId taskId) {
    super(projectId, taskId);
  }
}
