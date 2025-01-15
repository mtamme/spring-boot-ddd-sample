package com.github.task.management.infrastructure.messaging.project;

import com.github.task.management.domain.project.ProjectArchived;
import com.github.task.management.domain.project.ProjectNotificationService;
import com.github.task.management.domain.project.ProjectUnarchived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingProjectNotificationService implements ProjectNotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingProjectNotificationService.class);

  @Override
  public void notifyAboutProjectArchived(final ProjectArchived event) {
    LOGGER.info("notifyAboutProjectArchived: projectId={}", event.projectId());
  }

  @Override
  public void notifyAboutProjectUnarchived(final ProjectUnarchived event) {
    LOGGER.info("notifyAboutProjectUnarchived: projectId={}", event.projectId());
  }
}
