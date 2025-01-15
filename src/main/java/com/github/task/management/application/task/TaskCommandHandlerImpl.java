package com.github.task.management.application.task;

import com.github.task.management.application.task.command.AssignTaskCommand;
import com.github.task.management.application.task.command.CloseTaskCommand;
import com.github.task.management.application.task.command.OpenTaskCommand;
import com.github.task.management.application.task.command.StartTaskCommand;
import com.github.task.management.application.task.command.UnassignTaskCommand;
import com.github.task.management.domain.collaborator.Assignee;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.ProjectNotFoundException;
import com.github.task.management.domain.project.ProjectRepository;
import com.github.task.management.domain.task.TaskId;
import com.github.task.management.domain.task.TaskNotFoundException;
import com.github.task.management.domain.task.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Objects;

@Service
public class TaskCommandHandlerImpl implements TaskCommandHandler {

  private final Clock clock;
  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;

  public TaskCommandHandlerImpl(final Clock clock,
                                final ProjectRepository projectRepository,
                                final TaskRepository taskRepository) {
    this.clock = Objects.requireNonNull(clock);
    this.projectRepository = Objects.requireNonNull(projectRepository);
    this.taskRepository = Objects.requireNonNull(taskRepository);
  }

  @Override
  public String openTask(final OpenTaskCommand command) {
    final var project = projectRepository.findByProjectId(new ProjectId(command.projectId()))
      .orElseThrow(ProjectNotFoundException::new);
    final var task = project.openTask(
      taskRepository.nextTaskId(),
      clock.instant(),
      command.summary(),
      command.description());

    taskRepository.save(task);

    return task.taskId()
      .value();
  }

  @Override
  public void assignTask(final AssignTaskCommand command) {
    final var task = taskRepository.findByTaskId(new TaskId(command.taskId()))
      .orElseThrow(TaskNotFoundException::new);

    task.assign(new Assignee(command.assigneeName()));
    taskRepository.save(task);
  }

  @Override
  public void unassignTask(final UnassignTaskCommand command) {
    final var task = taskRepository.findByTaskId(new TaskId(command.taskId()))
      .orElseThrow(TaskNotFoundException::new);

    task.unassign(new Assignee(command.assigneeName()));
    taskRepository.save(task);
  }

  @Override
  public void startTask(final StartTaskCommand command) {
    final var task = taskRepository.findByTaskId(new TaskId(command.taskId()))
      .orElseThrow(TaskNotFoundException::new);

    task.start(clock.instant());
    taskRepository.save(task);
  }

  @Override
  public void closeTask(final CloseTaskCommand command) {
    final var task = taskRepository.findByTaskId(new TaskId(command.taskId()))
      .orElseThrow(TaskNotFoundException::new);

    task.close(clock.instant());
    taskRepository.save(task);
  }
}
