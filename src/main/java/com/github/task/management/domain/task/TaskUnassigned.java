package com.github.task.management.domain.task;

import com.github.task.management.domain.project.ProjectId;

public class TaskUnassigned extends TaskEvent {

  public TaskUnassigned(final ProjectId projectId, final TaskId taskId) {
    super(projectId, taskId);
  }
}
