package com.github.task.management.infrastructure.persistence.task;

import com.github.seedwork.infrastructure.persistence.Search;
import com.github.task.management.application.task.TaskQueryHandler;
import com.github.task.management.application.task.view.TaskDetailView;
import com.github.task.management.application.task.view.TaskSummaryView;
import com.github.task.management.domain.task.TaskNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JpaTaskQueryHandler implements TaskQueryHandler {

  private final JpaTaskRepository repository;

  public JpaTaskQueryHandler(final JpaTaskRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public TaskDetailView getTask(final String taskId) {
    return repository.find(taskId)
      .orElseThrow(TaskNotFoundException::new);
  }

  @Override
  public List<TaskSummaryView> listTasks(final long offset, final int limit) {
    return repository.findAll(offset, limit);
  }

  @Override
  public List<TaskSummaryView> searchTasks(final String query, final long offset, final int limit) {
    final var search = Search.of(query);

    return repository.findAllByPattern(search.containsPattern(), search.startsWithPattern(), offset, limit);
  }
}
