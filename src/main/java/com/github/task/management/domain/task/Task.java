package com.github.task.management.domain.task;

import com.github.seedwork.domain.AggregateRoot;
import com.github.task.management.domain.collaborator.Assignee;
import com.github.task.management.domain.project.Project;
import com.github.task.management.domain.project.ProjectId;

import java.time.Instant;
import java.util.Objects;

public class Task extends AggregateRoot {

  private ProjectId projectId;
  private TaskId taskId;
  private TaskStatus status;
  private Instant openedAt;
  private Instant startedAt;
  private Instant closedAt;
  private String summary;
  private String description;
  private Assignee assignee;

  public Task(final Project project,
              final TaskId taskId,
              final Instant openedAt,
              final String summary,
              final String description) {
    setProject(project);
    setTaskId(taskId);
    setSummary(summary);
    setDescription(description);

    open(project, openedAt);
  }

  private void open(final Project project, final Instant openedAt) {
    if (!canOpen(project)) {
      throw Problems.TASK_NOT_OPENABLE.toException();
    }
    markAsOpen(openedAt);
  }

  private boolean canOpen(final Project project) {
    return !project.isArchived();
  }

  private void markAsOpen(final Instant openedAt) {
    setStatus(TaskStatus.OPEN);
    setOpenedAt(openedAt);
    raiseEvent(new TaskOpened(projectId(), taskId()));
  }

  private void setProject(final Project project) {
    setProjectId(project.projectId());
  }

  public ProjectId projectId() {
    return projectId;
  }

  private void setProjectId(final ProjectId projectId) {
    this.projectId = Objects.requireNonNull(projectId);
  }

  public TaskId taskId() {
    return taskId;
  }

  private void setTaskId(final TaskId taskId) {
    this.taskId = Objects.requireNonNull(taskId);
  }

  public boolean isOpen() {
    return status().isOpen();
  }

  public boolean isStarted() {
    return status().isStarted();
  }

  public boolean isClosed() {
    return status().isClosed();
  }

  public TaskStatus status() {
    return status;
  }

  private void setStatus(final TaskStatus status) {
    this.status = Objects.requireNonNull(status);
  }

  public Instant openedAt() {
    return openedAt;
  }

  private void setOpenedAt(final Instant openedAt) {
    this.openedAt = Objects.requireNonNull(openedAt);
  }

  public Instant startedAt() {
    return startedAt;
  }

  private void setStartedAt(final Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant closedAt() {
    return closedAt;
  }

  private void setClosedAt(final Instant closedAt) {
    this.closedAt = closedAt;
  }

  public String summary() {
    return summary;
  }

  private void setSummary(final String summary) {
    this.summary = Objects.requireNonNull(summary);
  }

  public String description() {
    return description;
  }

  private void setDescription(final String description) {
    this.description = Objects.requireNonNull(description);
  }

  public boolean isAssigned() {
    return (assignee != null);
  }

  public boolean isAssignedTo(final Assignee assignee) {
    return Objects.equals(assignee, assignee());
  }

  public Assignee assignee() {
    return assignee;
  }

  private void setAssignee(final Assignee assignee) {
    this.assignee = assignee;
  }

  public void assign(final Assignee assignee) {
    Objects.requireNonNull(assignee);

    if (!canAssign()) {
      throw Problems.TASK_NOT_ASSIGNABLE.toException();
    }
    if (isAssignedTo(assignee)) {
      return;
    }
    setAssignee(assignee);
    raiseEvent(new TaskAssigned(projectId(), taskId(), assignee()));
  }

  private boolean canAssign() {
    return isOpen();
  }

  public void unassign(final Assignee assignee) {
    Objects.requireNonNull(assignee);

    if (!canUnassign()) {
      throw Problems.TASK_NOT_UNASSIGNABLE.toException();
    }
    if (!isAssignedTo(assignee)) {
      return;
    }
    setAssignee(null);
    raiseEvent(new TaskUnassigned(projectId(), taskId()));
  }

  private boolean canUnassign() {
    return isOpen();
  }

  public void start(final Instant startedAt) {
    Objects.requireNonNull(startedAt);

    if (!canStart()) {
      throw Problems.TASK_NOT_STARTABLE.toException();
    }
    if (isStarted()) {
      return;
    }
    markAsStarted(startedAt);
  }

  private boolean canStart() {
    return (isOpen() || isStarted()) && isAssigned();
  }

  private void markAsStarted(final Instant startedAt) {
    setStatus(TaskStatus.STARTED);
    setStartedAt(startedAt);
    raiseEvent(new TaskStarted(projectId(), taskId()));
  }

  public void close(final Instant closedAt) {
    Objects.requireNonNull(closedAt);

    if (!canClose()) {
      throw Problems.TASK_NOT_CLOSEABLE.toException();
    }
    if (isClosed()) {
      return;
    }
    markAsClosed(closedAt);
  }

  private boolean canClose() {
    return isStarted() || isClosed();
  }

  private void markAsClosed(final Instant closedAt) {
    setStatus(TaskStatus.CLOSED);
    setClosedAt(closedAt);
    raiseEvent(new TaskClosed(projectId(), taskId()));
  }

  private Long id;

  protected Task() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof Task other)) {
      return false;
    }

    return Objects.equals(other.taskId(), taskId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(taskId());
  }
}
