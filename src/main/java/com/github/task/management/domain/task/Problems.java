package com.github.task.management.domain.task;

import com.github.seedwork.core.problem.Problem;

public final class Problems {

  public static final Problem TASK_NOT_OPENABLE = new Problem("task-not-openable", "Task not openable");
  public static final Problem TASK_NOT_ASSIGNABLE = new Problem("task-not-assignable", "Task not assignable");
  public static final Problem TASK_NOT_CLOSEABLE = new Problem("task-not-closeable", "Task not closeable");
  public static final Problem TASK_NOT_STARTABLE = new Problem("task-not-startable", "Task not startable");
  public static final Problem TASK_NOT_UNASSIGNABLE = new Problem("task-not-unassignable", "Task not unassignable");

  private Problems() {
  }
}
