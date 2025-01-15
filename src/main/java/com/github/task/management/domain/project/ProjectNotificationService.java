package com.github.task.management.domain.project;

public interface ProjectNotificationService {

  void notifyAboutProjectArchived(ProjectArchived event);

  void notifyAboutProjectUnarchived(ProjectUnarchived event);
}
