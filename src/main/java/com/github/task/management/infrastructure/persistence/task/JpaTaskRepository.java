package com.github.task.management.infrastructure.persistence.task;

import com.github.seedwork.core.util.Longs;
import com.github.seedwork.infrastructure.persistence.JpaAggregateRepository;
import com.github.task.management.application.task.view.TaskDetailView;
import com.github.task.management.application.task.view.TaskSummaryView;
import com.github.task.management.domain.task.Task;
import com.github.task.management.domain.task.TaskId;
import com.github.task.management.domain.task.TaskRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaTaskRepository extends Repository<Task, Long>, JpaAggregateRepository<Task>, TaskRepository {

  @NativeQuery(name = "TaskDetailView.find")
  Optional<TaskDetailView> find(@Param("task_id") String taskId);

  @NativeQuery(name = "TaskSummaryView.findAll")
  List<TaskSummaryView> findAll(@Param("offset") long offset,
                                @Param("limit") int limit);

  @NativeQuery(name = "TaskSummaryView.findAllByPattern")
  List<TaskSummaryView> findAllByPattern(@Param("filter_pattern") String filterPattern,
                                         @Param("sort_pattern") String sortPattern,
                                         @Param("offset") long offset,
                                         @Param("limit") int limit);

  @Override
  default TaskId nextTaskId() {
    return new TaskId(Longs.toBase64String(Longs.nextLong()));
  }
}
