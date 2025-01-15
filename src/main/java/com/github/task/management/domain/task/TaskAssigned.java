package com.github.task.management.domain.task;

import com.github.task.management.domain.collaborator.Assignee;
import com.github.task.management.domain.project.ProjectId;

import java.util.Objects;

public class TaskAssigned extends TaskEvent {

  private final Assignee assignee;

  public TaskAssigned(final ProjectId projectId, final TaskId taskId, final Assignee assignee) {
    super(projectId, taskId);

    this.assignee = Objects.requireNonNull(assignee);
  }

  public Assignee assignee() {
    return assignee;
  }
}
