package com.github.task.management.domain.task;

import com.github.seedwork.domain.Event;
import com.github.task.management.domain.project.ProjectId;

import java.util.Objects;

public abstract class TaskEvent implements Event {

  private final ProjectId projectId;
  private final TaskId taskId;

  protected TaskEvent(final ProjectId projectId, final TaskId taskId) {
    this.projectId = Objects.requireNonNull(projectId);
    this.taskId = Objects.requireNonNull(taskId);
  }

  public ProjectId projectId() {
    return projectId;
  }

  public TaskId taskId() {
    return taskId;
  }
}
