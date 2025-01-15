package com.github.task.management.application.task;

import com.github.task.management.domain.task.TaskAssigned;
import com.github.task.management.domain.task.TaskClosed;
import com.github.task.management.domain.task.TaskNotificationService;
import com.github.task.management.domain.task.TaskOpened;
import com.github.task.management.domain.task.TaskStarted;
import com.github.task.management.domain.task.TaskUnassigned;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TaskEventHandler {

  private final TaskNotificationService taskNotificationService;

  public TaskEventHandler(final TaskNotificationService taskNotificationService) {
    this.taskNotificationService = Objects.requireNonNull(taskNotificationService);
  }

  @EventListener
  public void onTaskOpened(final TaskOpened event) {
    taskNotificationService.notifyAboutTaskOpened(event);
  }

  @EventListener
  public void onTaskAssigned(final TaskAssigned event) {
    taskNotificationService.notifyAboutTaskAssigned(event);
  }

  @EventListener
  public void onTaskUnassigned(final TaskUnassigned event) {
    taskNotificationService.notifyAboutTaskUnassigned(event);
  }

  @EventListener
  public void onTaskStarted(final TaskStarted event) {
    taskNotificationService.notifyAboutTaskStarted(event);
  }

  @EventListener
  public void onTaskClosed(final TaskClosed event) {
    taskNotificationService.notifyAboutTaskClosed(event);
  }
}
