package com.github.task.management.application.task.view;

public record TaskSummaryView(String taskId,
                              String status,
                              String summary,
                              TaskAssigneeView assignee,
                              TaskProjectView project) {

  public TaskSummaryView(final String taskId,
                         final String status,
                         final String summary,
                         final String assigneeName,
                         final String projectId,
                         final String projectName) {
    this(taskId,
      status,
      summary,
      TaskAssigneeView.of(assigneeName),
      TaskProjectView.of(projectId, projectName));
  }
}
