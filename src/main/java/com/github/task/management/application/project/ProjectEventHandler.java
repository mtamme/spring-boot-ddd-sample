package com.github.task.management.application.project;

import com.github.task.management.domain.project.ProjectArchived;
import com.github.task.management.domain.project.ProjectNotificationService;
import com.github.task.management.domain.project.ProjectUnarchived;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProjectEventHandler {

  private final ProjectNotificationService projectNotificationService;

  public ProjectEventHandler(final ProjectNotificationService projectNotificationService) {
    this.projectNotificationService = Objects.requireNonNull(projectNotificationService);
  }

  @EventListener
  public void onProjectArchived(final ProjectArchived event) {
    projectNotificationService.notifyAboutProjectArchived(event);
  }

  @EventListener
  public void onProjectUnarchived(final ProjectUnarchived event) {
    projectNotificationService.notifyAboutProjectUnarchived(event);
  }
}
