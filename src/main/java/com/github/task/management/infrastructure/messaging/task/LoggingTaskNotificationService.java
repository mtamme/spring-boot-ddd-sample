package com.github.task.management.infrastructure.messaging.task;

import com.github.task.management.domain.task.TaskAssigned;
import com.github.task.management.domain.task.TaskClosed;
import com.github.task.management.domain.task.TaskNotificationService;
import com.github.task.management.domain.task.TaskOpened;
import com.github.task.management.domain.task.TaskStarted;
import com.github.task.management.domain.task.TaskUnassigned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingTaskNotificationService implements TaskNotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingTaskNotificationService.class);

  @Override
  public void notifyAboutTaskOpened(final TaskOpened event) {
    LOGGER.info("notifyAboutTaskOpened: taskId={}", event.taskId());
  }

  @Override
  public void notifyAboutTaskAssigned(final TaskAssigned event) {
    LOGGER.info("notifyAboutTaskAssigned: taskId={}", event.taskId());
  }

  @Override
  public void notifyAboutTaskUnassigned(final TaskUnassigned event) {
    LOGGER.info("notifyAboutTaskUnassigned: taskId={}", event.taskId());
  }

  @Override
  public void notifyAboutTaskStarted(final TaskStarted event) {
    LOGGER.info("notifyAboutTaskStarted: taskId={}", event.taskId());
  }

  @Override
  public void notifyAboutTaskClosed(final TaskClosed event) {
    LOGGER.info("notifyAboutTaskClosed: taskId={}", event.taskId());
  }
}
