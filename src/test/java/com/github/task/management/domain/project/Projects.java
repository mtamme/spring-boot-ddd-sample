package com.github.task.management.domain.project;

import com.github.seedwork.core.util.Consumers;

public final class Projects {

  private Projects() {
  }

  public static Project newArchivedProject(final String projectId) {
    final var project = newProject(projectId);

    project.archive();
    project.dispatchEvents(Consumers.empty());

    return project;
  }

  public static Project newProject(final String projectId) {
    final var project = new Project(
      new ProjectId(projectId),
      "TestName",
      "TestDescription");

    project.dispatchEvents(Consumers.empty());

    return project;
  }
}
