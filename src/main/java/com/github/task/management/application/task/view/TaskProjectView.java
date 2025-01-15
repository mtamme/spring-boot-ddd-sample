package com.github.task.management.application.task.view;

public record TaskProjectView(String projectId, String name) {

  public static TaskProjectView of(final String projectId, final String name) {
    if (projectId == null) {
      return null;
    }

    return new TaskProjectView(projectId, name);
  }
}
