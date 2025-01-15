package com.github.task.management.api;

import com.github.task.management.api.representation.GetTaskResponse;
import com.github.task.management.api.representation.ListTasksResponse;
import com.github.task.management.api.representation.OpenTaskRequest;
import com.github.task.management.api.representation.OpenTaskResponse;
import com.github.task.management.api.representation.SearchTasksResponse;
import com.github.task.management.api.representation.TaskAssignee;
import com.github.task.management.api.representation.TaskProject;
import com.github.task.management.api.representation.TaskSummary;
import com.github.task.management.application.task.TaskCommandHandler;
import com.github.task.management.application.task.TaskQueryHandler;
import com.github.task.management.application.task.command.AssignTaskCommand;
import com.github.task.management.application.task.command.CloseTaskCommand;
import com.github.task.management.application.task.command.OpenTaskCommand;
import com.github.task.management.application.task.command.StartTaskCommand;
import com.github.task.management.application.task.command.UnassignTaskCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
public class TaskController implements TaskOperations {

  private final TaskCommandHandler taskCommandHandler;
  private final TaskQueryHandler taskQueryHandler;

  public TaskController(final TaskCommandHandler taskCommandHandler, final TaskQueryHandler taskQueryHandler) {
    this.taskCommandHandler = Objects.requireNonNull(taskCommandHandler);
    this.taskQueryHandler = Objects.requireNonNull(taskQueryHandler);
  }

  @Override
  public ResponseEntity<GetTaskResponse> getTask(final String taskId) {
    final var task = taskQueryHandler.getTask(taskId);
    final var response = new GetTaskResponse()
      .taskId(task.taskId())
      .status(task.status())
      .openedAt(task.openedAt())
      .startedAt(task.startedAt())
      .closedAt(task.closedAt())
      .summary(task.summary())
      .description(task.description())
      .assignee(Optional.ofNullable(task.assignee())
        .map(ta -> new TaskAssignee()
          .name(ta.name()))
        .orElse(null))
      .project(Optional.ofNullable(task.project())
        .map(tp -> new TaskProject()
          .projectId(tp.projectId())
          .name(tp.name()))
        .orElse(null));

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ListTasksResponse> listTasks(final Long offset, final Integer limit) {
    final var tasks = taskQueryHandler.listTasks(offset, limit)
      .stream()
      .map(t -> new TaskSummary()
        .taskId(t.taskId())
        .status(t.status())
        .summary(t.summary())
        .assignee(Optional.ofNullable(t.assignee())
          .map(ta -> new TaskAssignee()
            .name(ta.name()))
          .orElse(null))
        .project(Optional.ofNullable(t.project())
          .map(tp -> new TaskProject()
            .projectId(tp.projectId())
            .name(tp.name()))
          .orElse(null)))
      .toList();
    final var response = new ListTasksResponse()
      .tasks(tasks);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<OpenTaskResponse> openTask(final String projectId, final OpenTaskRequest request) {
    final var taskId = taskCommandHandler.openTask(new OpenTaskCommand(projectId, request.getSummary(), request.getDescription()));
    final var response = new OpenTaskResponse()
      .taskId(taskId);

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(response);
  }

  @Override
  public ResponseEntity<Void> assignTask(final String taskId, final String assigneeName) {
    taskCommandHandler.assignTask(new AssignTaskCommand(taskId, assigneeName));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> unassignTask(final String taskId, final String assigneeName) {
    taskCommandHandler.unassignTask(new UnassignTaskCommand(taskId, assigneeName));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> startTask(final String taskId) {
    taskCommandHandler.startTask(new StartTaskCommand(taskId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> closeTask(final String taskId) {
    taskCommandHandler.closeTask(new CloseTaskCommand(taskId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<SearchTasksResponse> searchTasks(final String query, final Long offset, final Integer limit) {
    final var tasks = taskQueryHandler.searchTasks(query, offset, limit)
      .stream()
      .map(t -> new TaskSummary()
        .taskId(t.taskId())
        .status(t.status())
        .summary(t.summary())
        .assignee(Optional.ofNullable(t.assignee())
          .map(ta -> new TaskAssignee()
            .name(ta.name()))
          .orElse(null))
        .project(Optional.ofNullable(t.project())
          .map(tp -> new TaskProject()
            .projectId(tp.projectId())
            .name(tp.name()))
          .orElse(null)))
      .toList();
    final var response = new SearchTasksResponse()
      .tasks(tasks);

    return ResponseEntity.ok(response);
  }
}
