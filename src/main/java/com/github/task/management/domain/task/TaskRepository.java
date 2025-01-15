package com.github.task.management.domain.task;

import java.util.Optional;

public interface TaskRepository {

  TaskId nextTaskId();

  Optional<Task> findByTaskId(TaskId taskId);

  void save(Task task);
}
