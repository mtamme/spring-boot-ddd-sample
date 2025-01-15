package com.github.task.management.application.task;

import com.github.task.management.application.task.command.AssignTaskCommand;
import com.github.task.management.application.task.command.CloseTaskCommand;
import com.github.task.management.application.task.command.OpenTaskCommand;
import com.github.task.management.application.task.command.StartTaskCommand;
import com.github.task.management.application.task.command.UnassignTaskCommand;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.ProjectRepository;
import com.github.task.management.domain.project.Projects;
import com.github.task.management.domain.task.Task;
import com.github.task.management.domain.task.TaskId;
import com.github.task.management.domain.task.TaskRepository;
import com.github.task.management.domain.task.TaskStatus;
import com.github.task.management.domain.task.Tasks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskCommandHandlerImplTest {

  @Mock
  private Clock clock;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private TaskRepository taskRepository;

  @Test
  void openTaskShouldOpenTaskAndReturnTaskId() {
    // Arrange
    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    Mockito.when(projectRepository.findByProjectId(new ProjectId("00000000001")))
      .thenReturn(Optional.of(Projects.newProject("00000000001")));
    Mockito.when(taskRepository.nextTaskId())
      .thenReturn(new TaskId("00000000000"));
    final var taskCaptor = ArgumentCaptor.forClass(Task.class);

    Mockito.doNothing()
      .when(taskRepository)
      .save(taskCaptor.capture());
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new OpenTaskCommand(
      "00000000001",
      "TestSummary",
      "TestDescription");

    // Act
    final var taskId = taskCommandHandler.openTask(command);

    // Assert
    assertEquals("00000000000", taskId);
    final var task = taskCaptor.getValue();

    assertEquals(new ProjectId("00000000001"), task.projectId());
    assertEquals(new TaskId("00000000000"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH, task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertNull(task.assignee());
  }

  @Test
  void assignTaskShouldAssignTask() {
    // Arrange
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newOpenTask("00000000001", "00000000000")));
    final var taskCaptor = ArgumentCaptor.forClass(Task.class);

    Mockito.doNothing()
      .when(taskRepository)
      .save(taskCaptor.capture());
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new AssignTaskCommand("00000000000", "TestName");

    // Act
    taskCommandHandler.assignTask(command);

    // Assert
    final var task = taskCaptor.getValue();

    assertEquals(new ProjectId("00000000001"), task.projectId());
    assertEquals(new TaskId("00000000000"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals("TestName", task.assignee().name());
  }

  @Test
  void unassignTaskShouldUnassignTask() {
    // Arrange
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newAssignedTask("00000000001", "00000000000")));
    final var taskCaptor = ArgumentCaptor.forClass(Task.class);

    Mockito.doNothing()
      .when(taskRepository)
      .save(taskCaptor.capture());
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new UnassignTaskCommand("00000000000", "TestName");

    // Act
    taskCommandHandler.unassignTask(command);

    // Assert
    final var task = taskCaptor.getValue();

    assertEquals(new ProjectId("00000000001"), task.projectId());
    assertEquals(new TaskId("00000000000"), task.taskId());
    assertEquals(TaskStatus.OPEN, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertNull(task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertNull(task.assignee());
  }

  @Test
  void startTaskShouldStartTask() {
    // Arrange
    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newAssignedTask("00000000001", "00000000000")));
    final var taskCaptor = ArgumentCaptor.forClass(Task.class);

    Mockito.doNothing()
      .when(taskRepository)
      .save(taskCaptor.capture());
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new StartTaskCommand("00000000000");

    // Act
    taskCommandHandler.startTask(command);

    // Assert
    final var task = taskCaptor.getValue();

    assertEquals(new ProjectId("00000000001"), task.projectId());
    assertEquals(new TaskId("00000000000"), task.taskId());
    assertEquals(TaskStatus.STARTED, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertEquals(Instant.EPOCH, task.startedAt());
    assertNull(task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals("TestName", task.assignee().name());
  }

  @Test
  void closeTaskShouldCloseTask() {
    // Arrange
    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newStartedTask("00000000001", "00000000000")));
    final var taskCaptor = ArgumentCaptor.forClass(Task.class);

    Mockito.doNothing()
      .when(taskRepository)
      .save(taskCaptor.capture());
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new CloseTaskCommand("00000000000");

    // Act
    taskCommandHandler.closeTask(command);

    // Assert
    final var task = taskCaptor.getValue();

    assertEquals(new ProjectId("00000000001"), task.projectId());
    assertEquals(new TaskId("00000000000"), task.taskId());
    assertEquals(TaskStatus.CLOSED, task.status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.openedAt());
    assertEquals(Instant.EPOCH.minusSeconds(2L), task.startedAt());
    assertEquals(Instant.EPOCH, task.closedAt());
    assertEquals("TestSummary", task.summary());
    assertEquals("TestDescription", task.description());
    assertEquals("TestName", task.assignee().name());
  }
}
