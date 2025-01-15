package com.github.task.management.api;

import com.github.seedwork.api.ControllerTest;
import com.github.task.management.api.representation.GetTaskResponse;
import com.github.task.management.api.representation.ListTasksResponse;
import com.github.task.management.api.representation.OpenTaskRequest;
import com.github.task.management.api.representation.OpenTaskResponse;
import com.github.task.management.api.representation.SearchTasksResponse;
import com.github.task.management.application.task.TaskCommandHandler;
import com.github.task.management.application.task.TaskQueryHandler;
import com.github.task.management.application.task.command.AssignTaskCommand;
import com.github.task.management.application.task.command.CloseTaskCommand;
import com.github.task.management.application.task.command.OpenTaskCommand;
import com.github.task.management.application.task.command.StartTaskCommand;
import com.github.task.management.application.task.command.UnassignTaskCommand;
import com.github.task.management.application.task.view.TaskDetailView;
import com.github.task.management.application.task.view.TaskSummaryView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest extends ControllerTest {

  @MockitoBean
  private TaskCommandHandler taskCommandHandler;
  @MockitoBean
  private TaskQueryHandler taskQueryHandler;

  @Test
  void openTaskShouldReturnOpenTaskResponse() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(OpenTaskCommand.class);

    Mockito.when(taskCommandHandler.openTask(commandCaptor.capture()))
      .thenReturn("00000000000");

    // Act
    final var entity = client().post()
      .uri("/projects/{project_id}/tasks", "00000000001")
      .body(new OpenTaskRequest("TestSummary", "TestDescription"))
      .retrieve()
      .toEntity(OpenTaskResponse.class);

    // Assert
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("00000000000", response.getTaskId());
    final var command = commandCaptor.getValue();

    assertEquals("00000000001", command.projectId());
    assertEquals("TestSummary", command.summary());
    assertEquals("TestDescription", command.description());
  }

  @Test
  void assignTaskShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(AssignTaskCommand.class);

    Mockito.doNothing()
      .when(taskCommandHandler)
      .assignTask(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/tasks/{task_id}/assignees/{assignee_name}", "00000000000", "TestName")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("00000000000", command.taskId());
    assertEquals("TestName", command.assigneeName());
  }

  @Test
  void unassignTaskShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(UnassignTaskCommand.class);

    Mockito.doNothing()
      .when(taskCommandHandler)
      .unassignTask(commandCaptor.capture());

    // Act
    final var entity = client().delete()
      .uri("/tasks/{task_id}/assignees/{assignee_name}", "00000000000", "TestName")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("00000000000", command.taskId());
    assertEquals("TestName", command.assigneeName());
  }

  @Test
  void startTaskShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(StartTaskCommand.class);

    Mockito.doNothing()
      .when(taskCommandHandler)
      .startTask(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/started-tasks/{task_id}", "00000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("00000000000", command.taskId());
  }

  @Test
  void closeTaskShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(CloseTaskCommand.class);

    Mockito.doNothing()
      .when(taskCommandHandler)
      .closeTask(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/closed-tasks/{task_id}", "00000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("00000000000", command.taskId());
  }

  @Test
  void getTaskShouldReturnGetTaskResponse() {
    // Arrange
    Mockito.when(taskQueryHandler.getTask("00000000000"))
      .thenReturn(new TaskDetailView(
        "00000000000",
        "OPEN",
        Instant.EPOCH,
        null,
        null,
        "TestSummary",
        "TestDescription",
        null,
        "00000000001",
        "TestName"));

    // Act
    final var entity = client().get()
      .uri("/tasks/{task_id}", "00000000000")
      .retrieve()
      .toEntity(GetTaskResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("00000000000", response.getTaskId());
    assertEquals("OPEN", response.getStatus());
    assertEquals("TestSummary", response.getSummary());
    assertEquals("TestDescription", response.getDescription());
    assertNull(response.getAssignee());
    final var project = response.getProject();

    assertNotNull(project);
    assertEquals("00000000001", project.getProjectId());
    assertEquals("TestName", project.getName());
  }

  @Test
  void listTasksShouldReturnListTasksResponse() {
    // Arrange
    Mockito.when(taskQueryHandler.listTasks(0, 10))
      .thenReturn(List.of(new TaskSummaryView(
        "00000000000",
        "OPEN",
        "TestSummary",
        null,
        "00000000001",
        "TestName")));

    // Act
    final var entity = client().get()
      .uri("/tasks")
      .retrieve()
      .toEntity(ListTasksResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var tasks = response.getTasks();

    assertEquals(1, tasks.size());
    final var task = tasks.getFirst();

    assertEquals("00000000000", task.getTaskId());
    assertEquals("OPEN", task.getStatus());
    assertEquals("TestSummary", task.getSummary());
    assertNull(task.getAssignee());
    final var project = task.getProject();

    assertEquals("00000000001", project.getProjectId());
    assertEquals("TestName", project.getName());
  }

  @Test
  void searchTasksShouldReturnSearchTasksResponse() {
    // Arrange
    Mockito.when(taskQueryHandler.searchTasks("test name", 0, 10))
      .thenReturn(List.of(new TaskSummaryView(
        "00000000000",
        "OPEN",
        "TestSummary",
        null,
        "00000000001",
        "TestName")));

    // Act
    final var entity = client().get()
      .uri("/search/tasks?q={query}", "test name")
      .retrieve()
      .toEntity(SearchTasksResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var tasks = response.getTasks();

    assertEquals(1, tasks.size());
    final var task = tasks.getFirst();

    assertEquals("00000000000", task.getTaskId());
    assertEquals("OPEN", task.getStatus());
    assertEquals("TestSummary", task.getSummary());
    assertNull(task.getAssignee());
    final var project = task.getProject();

    assertEquals("00000000001", project.getProjectId());
    assertEquals("TestName", project.getName());
  }
}
