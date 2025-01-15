package com.github.task.management.domain.task;

import com.github.seedwork.core.problem.ProblemException;
import com.github.task.management.domain.collaborator.Assignee;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.Projects;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

  @Test
  void newTaskWithNewProjectShouldReturnOpenTaskAndRaiseTaskOpenedEvent() {
    // Arrange
    // Act
    final var task = new Task(
      Projects.newProject("00000000000"),
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

  @Test
  void newTaskWithArchivedProjectShouldThrowProblemExceptionWithTaskNotOpenableProblem() {
    // Arrange
    // Act
    final var exception = assertThrows(ProblemException.class, () -> new Task(
      Projects.newArchivedProject("00000000000"),
      new TaskId("00000000001"),
      Instant.EPOCH,
      "TestSummary",
      "TestDescription")
    );

    // Assert
    assertEquals(Problems.TASK_NOT_OPENABLE, exception.getProblem());
  }

  @Test
  void assignWithOpenTaskShouldReturnAssignedTaskAndRaiseTaskAssignedEvent() {
    // Arrange
    final var task = Tasks.newOpenTask("00000000000", "00000000001");

    // Act
    task.assign(new Assignee("TestName"));

    // Assert
    assertEquals(1, task.events().size());
    assertEquals(TaskAssigned.class, task.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("TestName"), task.assignee());
  }

  @Test
  void assignWithAssignedTaskAndSameAssigneeShouldReturnAssignedTaskAndRaiseNoEvent() {
    // Arrange
    final var task = Tasks.newAssignedTask("00000000000", "00000000001");

    // Act
    task.assign(task.assignee());

    // Assert
    assertEquals(0, task.events().size());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("TestName"), task.assignee());
  }

  @Test
  void assignWithAssignedTaskAndOtherAssigneeShouldReturnAssignedTaskAndRaiseTaskAssignedEvent() {
    // Arrange
    final var task = Tasks.newAssignedTask("00000000000", "00000000001");

    // Act
    task.assign(new Assignee("OtherName"));

    // Assert
    assertEquals(1, task.events().size());
    assertEquals(TaskAssigned.class, task.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("OtherName"), task.assignee());
  }

  @Test
  void assignWithStartedTaskShouldThrowProblemExceptionWithTaskNotAssignableProblem() {
    // Arrange
    final var task = Tasks.newStartedTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.assign(new Assignee("OtherName")));

    assertEquals(Problems.TASK_NOT_ASSIGNABLE, exception.getProblem());
  }

  @Test
  void assignWithClosedTaskShouldThrowProblemExceptionWithTaskNotAssignableProblem() {
    // Arrange
    final var task = Tasks.newClosedTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.assign(new Assignee("OtherName")));

    assertEquals(Problems.TASK_NOT_ASSIGNABLE, exception.getProblem());
  }

  @Test
  void unassignWithOpenTaskShouldReturnOpenTaskAndRaiseNoEvent() {
    // Arrange
    final var task = Tasks.newOpenTask("00000000000", "00000000001");

    // Act
    task.unassign(new Assignee("TestName"));

    // Assert
    assertEquals(0, task.events().size());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertNull(task.assignee());
  }

  @Test
  void unassignWithAssignedTaskShouldReturnOpenTaskAndRaiseTaskUnassignedEvent() {
    // Arrange
    final var task = Tasks.newAssignedTask("00000000000", "00000000001");

    // Act
    task.unassign(new Assignee("TestName"));

    // Assert
    assertEquals(1, task.events().size());
    assertEquals(TaskUnassigned.class, task.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertNull(task.assignee());
  }

  @Test
  void unassignWithAssignedTaskAndOtherAssigneeShouldReturnAssignedTaskAndRaiseNoEvent() {
    // Arrange
    final var task = Tasks.newAssignedTask("00000000000", "00000000001");

    // Act
    task.unassign(new Assignee("OtherName"));

    // Assert
    assertEquals(0, task.events().size());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("TestName"), task.assignee());
  }

  @Test
  void unassignWithStartedTaskShouldThrowProblemExceptionWithTaskNotUnassignableProblem() {
    // Arrange
    final var task = Tasks.newStartedTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.unassign(new Assignee("TestName")));

    assertEquals(Problems.TASK_NOT_UNASSIGNABLE, exception.getProblem());
  }

  @Test
  void unassignWithClosedTaskShouldThrowProblemExceptionWithTaskNotUnassignableProblem() {
    // Arrange
    final var task = Tasks.newClosedTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.unassign(new Assignee("TestName")));

    assertEquals(Problems.TASK_NOT_UNASSIGNABLE, exception.getProblem());
  }

  @Test
  void startWithOpenTaskShouldThrowProblemExceptionWithTaskNotStartableProblem() {
    // Arrange
    final var task = Tasks.newOpenTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.start(Instant.EPOCH));

    assertEquals(Problems.TASK_NOT_STARTABLE, exception.getProblem());
  }

  @Test
  void startWithAssignedTaskShouldReturnStartedTaskAndRaiseTaskStartedEvent() {
    // Arrange
    final var task = Tasks.newAssignedTask("00000000000", "00000000001");

    // Act
    task.start(Instant.EPOCH);

    // Assert
    assertEquals(1, task.events().size());
    assertEquals(TaskStarted.class, task.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.STARTED, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertEquals(Instant.EPOCH, task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("TestName"), task.assignee());
  }

  @Test
  void startWithStartedTaskShouldReturnStartedTaskAndRaiseNoEvent() {
    // Arrange
    final var task = Tasks.newStartedTask("00000000000", "00000000001");

    // Act
    task.start(Instant.EPOCH);

    // Assert
    assertEquals(0, task.events().size());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.STARTED, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertEquals(Instant.EPOCH.minusSeconds(2L), task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("TestName"), task.assignee());
  }

  @Test
  void startWithClosedTaskShouldThrowProblemExceptionWithTaskNotStartableProblem() {
    // Arrange
    final var task = Tasks.newClosedTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.start(Instant.EPOCH));

    assertEquals(Problems.TASK_NOT_STARTABLE, exception.getProblem());
  }

  @Test
  void closeWithOpenTaskShouldThrowProblemExceptionWithTaskNotClosableProblem() {
    // Arrange
    final var task = Tasks.newOpenTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.close(Instant.EPOCH));

    assertEquals(Problems.TASK_NOT_CLOSEABLE, exception.getProblem());
  }

  @Test
  void closeWithAssignedTaskShouldThrowProblemExceptionWithTaskNotClosableProblem() {
    // Arrange
    final var task = Tasks.newAssignedTask("00000000000", "00000000001");

    // Act
    // Assert
    final var exception = assertThrows(ProblemException.class, () -> task.close(Instant.EPOCH));

    assertEquals(Problems.TASK_NOT_CLOSEABLE, exception.getProblem());
  }

  @Test
  void closeWithStartedTaskShouldReturnClosedTaskAndRaiseTaskClosedEvent() {
    // Arrange
    final var task = Tasks.newStartedTask("00000000000", "00000000001");

    // Act
    task.close(Instant.EPOCH);

    // Assert
    assertEquals(1, task.events().size());
    assertEquals(TaskClosed.class, task.events().getFirst().getClass());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.CLOSED, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertEquals(Instant.EPOCH.minusSeconds(2L), task.startedAt());
    assertEquals(Instant.EPOCH, task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("TestName"), task.assignee());
  }

  @Test
  void closeWithClosedTaskShouldReturnClosedTaskAndRaiseNoEvent() {
    // Arrange
    final var task = Tasks.newClosedTask("00000000000", "00000000001");

    // Act
    task.close(Instant.EPOCH);

    // Assert
    assertEquals(0, task.events().size());
    assertEquals(new ProjectId("00000000000"), task.projectId());
    assertEquals(new TaskId("00000000001"), task.taskId());
    assertEquals(TaskStatus.CLOSED, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertEquals(Instant.EPOCH.minusSeconds(2L), task.startedAt());
    assertEquals(Instant.EPOCH.minusSeconds(1L), task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals(new Assignee("TestName"), task.assignee());
  }
}
