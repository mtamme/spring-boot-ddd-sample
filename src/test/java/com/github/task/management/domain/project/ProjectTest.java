package com.github.task.management.domain.project;

import com.github.task.management.domain.task.TaskId;
import com.github.task.management.domain.task.TaskOpened;
import com.github.task.management.domain.task.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

  @Test
  void newProjectShouldReturnNewProject() {
    // Arrange
    // Act
    final var project = new Project(
      new ProjectId("00000000000"),
      "TestName",
      "TestDescription");

    // Assert
    assertEquals(0, project.events().size());
    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertFalse(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void archiveWithNewProjectShouldReturnArchivedProjectAndRaiseProjectArchivedEvent() {
    // Arrange
    final var project = Projects.newProject("00000000000");

    // Act
    project.archive();

    // Assert
    assertEquals(1, project.events().size());
    assertEquals(ProjectArchived.class, project.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertTrue(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void archiveWithArchivedProjectShouldReturnArchivedProjectAndRaiseProjectArchivedEvent() {
    // Arrange
    final var project = Projects.newArchivedProject("00000000000");

    // Act
    project.archive();

    // Assert
    assertEquals(0, project.events().size());
    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertTrue(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void unarchiveWithNewProjectShouldReturnArchivedProjectAndRaiseProjectArchivedEvent() {
    // Arrange
    final var project = Projects.newProject("00000000000");

    // Act
    project.unarchive();

    // Assert
    assertEquals(0, project.events().size());
    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertFalse(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void unarchiveWithArchivedProjectShouldReturnArchivedProjectAndRaiseProjectArchivedEvent() {
    // Arrange
    final var project = Projects.newArchivedProject("00000000000");

    // Act
    project.unarchive();

    // Assert
    assertEquals(1, project.events().size());
    assertEquals(ProjectUnarchived.class, project.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertFalse(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void openTaskShouldReturnOpenTaskAndRaiseTaskOpenedEvent() {
    // Arrange
    final var project = Projects.newProject("00000000000");

    // Act
    final var task = project.openTask(
      new TaskId("00000000001"),
      Instant.EPOCH,
      "TestSummary",
      "TestDescription");

    // Assert
    assertEquals(1, task.events().size());
    assertEquals(TaskOpened.class, task.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH, task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertNull(task.assignee());
  }
}
