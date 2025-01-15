package com.github.task.management.domain.project;

public class ProjectArchived extends ProjectEvent {

  public ProjectArchived(final ProjectId projectId) {
    super(projectId);
  }
}
