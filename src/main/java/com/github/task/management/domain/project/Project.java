package com.github.task.management.domain.project;

import com.github.seedwork.domain.AggregateRoot;
import com.github.task.management.domain.task.Task;
import com.github.task.management.domain.task.TaskId;

import java.time.Instant;
import java.util.Objects;

public class Project extends AggregateRoot {

  private ProjectId projectId;
  private boolean archived;
  private String name;
  private String description;

  public Project(final ProjectId projectId,
                 final String name,
                 final String description) {
    setProjectId(projectId);
    setArchived(false);
    setName(name);
    setDescription(description);
  }

  public ProjectId projectId() {
    return projectId;
  }

  private void setProjectId(final ProjectId projectId) {
    this.projectId = Objects.requireNonNull(projectId);
  }

  public boolean isArchived() {
    return archived;
  }

  private void setArchived(final boolean archived) {
    this.archived = archived;
  }

  public String name() {
    return name;
  }

  private void setName(final String name) {
    this.name = Objects.requireNonNull(name);
  }

  public String description() {
    return description;
  }

  private void setDescription(final String description) {
    this.description = Objects.requireNonNull(description);
  }

  public void archive() {
    if (!canArchive()) {
      throw Problems.PROJECT_NOT_ARCHIVABLE.toException();
    }
    if (isArchived()) {
      return;
    }
    markAsArchived();
  }

  private boolean canArchive() {
    return true;
  }

  private void markAsArchived() {
    setArchived(true);
    raiseEvent(new ProjectArchived(projectId()));
  }

  public void unarchive() {
    if (!canUnarchive()) {
      throw Problems.PROJECT_NOT_UNARCHIVABLE.toException();
    }
    if (!isArchived()) {
      return;
    }
    markAsUnarchived();
  }

  private boolean canUnarchive() {
    return true;
  }

  private void markAsUnarchived() {
    setArchived(false);
    raiseEvent(new ProjectUnarchived(projectId()));
  }

  public Task openTask(final TaskId taskId,
                       final Instant openedAt,
                       final String summary,
                       final String description) {
    return new Task(this, taskId, openedAt, summary, description);
  }

  private Long id;

  protected Project() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof Project other)) {
      return false;
    }

    return Objects.equals(other.projectId(), projectId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(projectId());
  }
}
