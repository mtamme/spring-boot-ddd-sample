package com.github.task.management.application.task;

import com.github.task.management.application.task.command.AssignTaskCommand;
import com.github.task.management.application.task.command.CloseTaskCommand;
import com.github.task.management.application.task.command.OpenTaskCommand;
import com.github.task.management.application.task.command.StartTaskCommand;
import com.github.task.management.application.task.command.UnassignTaskCommand;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.ProjectRepository;
import com.github.task.management.domain.project.Projects;
import com.github.task.management.domain.task.TaskId;
import com.github.task.management.domain.task.TaskRepository;
import com.github.task.management.domain.task.TaskStatus;
import com.github.task.management.domain.task.Tasks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    Mockito.doNothing()
      .when(taskRepository)
      .save(Mockito.assertArg(t -> {
        assertEquals(new ProjectId("00000000001"), t.projectId());
        assertEquals(new TaskId("00000000000"), t.taskId());
        assertEquals(TaskStatus.OPEN, t.status());
        assertEquals(Instant.EPOCH, t.openedAt());
        assertNull(t.startedAt());
        assertNull(t.closedAt());
        assertEquals("TestSummary", t.summary());
        assertEquals("TestDescription", t.description());
        assertNull(t.assignee());
      }));
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new OpenTaskCommand(
      "00000000001",
      "TestSummary",
      "TestDescription");

    // Act
    final var taskId = taskCommandHandler.openTask(command);

    // Assert
    assertEquals("00000000000", taskId);
  }

  @Test
  void assignTaskShouldAssignTask() {
    // Arrange
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newOpenTask("00000000001", "00000000000")));
    Mockito.doNothing()
      .when(taskRepository)
      .save(Mockito.assertArg(t -> {
        assertEquals(new ProjectId("00000000001"), t.projectId());
        assertEquals(new TaskId("00000000000"), t.taskId());
        assertEquals(TaskStatus.OPEN, t.status());
        assertEquals(Instant.EPOCH.minusSeconds(3L), t.openedAt());
        assertNull(t.startedAt());
        assertNull(t.closedAt());
        assertEquals("TestSummary", t.summary());
        assertEquals("TestDescription", t.description());
        assertEquals("TestName", t.assignee().name());
      }));
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new AssignTaskCommand("00000000000", "TestName");

    // Act
    // Assert
    taskCommandHandler.assignTask(command);
  }

  @Test
  void unassignTaskShouldUnassignTask() {
    // Arrange
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newAssignedTask("00000000001", "00000000000")));
    Mockito.doNothing()
      .when(taskRepository)
      .save(Mockito.assertArg(t -> {
        assertEquals(new ProjectId("00000000001"), t.projectId());
        assertEquals(new TaskId("00000000000"), t.taskId());
        assertEquals(TaskStatus.OPEN, t.status());
        assertEquals(Instant.EPOCH.minusSeconds(3L), t.openedAt());
        assertNull(t.startedAt());
        assertNull(t.closedAt());
        assertEquals("TestSummary", t.summary());
        assertEquals("TestDescription", t.description());
        assertNull(t.assignee());
      }));
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new UnassignTaskCommand("00000000000", "TestName");

    // Act
    // Assert
    taskCommandHandler.unassignTask(command);
  }

  @Test
  void startTaskShouldStartTask() {
    // Arrange
    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newAssignedTask("00000000001", "00000000000")));
    Mockito.doNothing()
      .when(taskRepository)
      .save(Mockito.assertArg(t -> {
        assertEquals(new ProjectId("00000000001"), t.projectId());
        assertEquals(new TaskId("00000000000"), t.taskId());
        assertEquals(TaskStatus.STARTED, t.status());
        assertEquals(Instant.EPOCH.minusSeconds(3L), t.openedAt());
        assertEquals(Instant.EPOCH, t.startedAt());
        assertNull(t.closedAt());
        assertEquals("TestSummary", t.summary());
        assertEquals("TestDescription", t.description());
        assertEquals("TestName", t.assignee().name());
      }));
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new StartTaskCommand("00000000000");

    // Act
    // Assert
    taskCommandHandler.startTask(command);
  }

  @Test
  void closeTaskShouldCloseTask() {
    // Arrange
    Mockito.when(clock.instant())
      .thenReturn(Instant.EPOCH);
    Mockito.when(taskRepository.findByTaskId(new TaskId("00000000000")))
      .thenReturn(Optional.of(Tasks.newStartedTask("00000000001", "00000000000")));
    Mockito.doNothing()
      .when(taskRepository)
      .save(Mockito.assertArg(t -> {
        assertEquals(new ProjectId("00000000001"), t.projectId());
        assertEquals(new TaskId("00000000000"), t.taskId());
        assertEquals(TaskStatus.CLOSED, t.status());
        assertEquals(Instant.EPOCH.minusSeconds(3L), t.openedAt());
        assertEquals(Instant.EPOCH.minusSeconds(2L), t.startedAt());
        assertEquals(Instant.EPOCH, t.closedAt());
        assertEquals("TestSummary", t.summary());
        assertEquals("TestDescription", t.description());
        assertEquals("TestName", t.assignee().name());
      }));
    final var taskCommandHandler = new TaskCommandHandlerImpl(clock, projectRepository, taskRepository);
    final var command = new CloseTaskCommand("00000000000");

    // Act
    // Assert
    taskCommandHandler.closeTask(command);
  }
}
