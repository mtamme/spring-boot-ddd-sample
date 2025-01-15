package com.github.task.management.domain.task;

public interface TaskNotificationService {

  void notifyAboutTaskOpened(TaskOpened event);

  void notifyAboutTaskAssigned(TaskAssigned event);

  void notifyAboutTaskUnassigned(TaskUnassigned event);

  void notifyAboutTaskStarted(TaskStarted event);

  void notifyAboutTaskClosed(TaskClosed event);
}
