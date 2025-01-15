package com.github.task.management.domain.project;

import com.github.seedwork.domain.Event;

import java.util.Objects;

public abstract class ProjectEvent implements Event {

  private final ProjectId projectId;

  protected ProjectEvent(final ProjectId projectId) {
    this.projectId = Objects.requireNonNull(projectId);
  }

  public ProjectId projectId() {
    return projectId;
  }
}
