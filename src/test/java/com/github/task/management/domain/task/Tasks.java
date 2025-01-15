package com.github.task.management.domain.task;

import com.github.seedwork.core.util.Consumers;
import com.github.task.management.domain.collaborator.Assignee;
import com.github.task.management.domain.project.Projects;

import java.time.Instant;

public final class Tasks {

  private Tasks() {
  }

  public static Task newClosedTask(final String projectId, final String taskId) {
    final var task = newStartedTask(projectId, taskId);

    task.close(Instant.EPOCH.minusSeconds(1L));
    task.dispatchEvents(Consumers.empty());

    return task;
  }

  public static Task newStartedTask(final String projectId, final String taskId) {
    final var task = newAssignedTask(projectId, taskId);

    task.start(Instant.EPOCH.minusSeconds(2L));
    task.dispatchEvents(Consumers.empty());

    return task;
  }

  public static Task newAssignedTask(final String projectId, final String taskId) {
    final var task = newOpenTask(projectId, taskId);

    task.assign(new Assignee("TestName"));
    task.dispatchEvents(Consumers.empty());

    return task;
  }

  public static Task newOpenTask(final String projectId, final String taskId) {
    final var task = new Task(
      Projects.newProject(projectId),
      new TaskId(taskId),
      Instant.EPOCH.minusSeconds(3L),
      "TestSummary",
      "TestDescription");

    task.dispatchEvents(Consumers.empty());

    return task;
  }
}
