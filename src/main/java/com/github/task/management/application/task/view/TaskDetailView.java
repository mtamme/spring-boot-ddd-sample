package com.github.task.management.application.task.view;

import java.time.Instant;

public record TaskDetailView(String taskId,
                             String status,
                             Instant openedAt,
                             Instant startedAt,
                             Instant closedAt,
                             String summary,
                             String description,
                             TaskAssigneeView assignee,
                             TaskProjectView project) {

  public TaskDetailView(final String taskId,
                        final String status,
                        final Instant openedAt,
                        final Instant startedAt,
                        final Instant closedAt,
                        final String summary,
                        final String description,
                        final String assigneeName,
                        final String projectId,
                        final String projectName) {
    this(taskId,
      status,
      openedAt,
      startedAt,
      closedAt,
      summary,
      description,
      TaskAssigneeView.of(assigneeName),
      TaskProjectView.of(projectId, projectName));
  }
}
