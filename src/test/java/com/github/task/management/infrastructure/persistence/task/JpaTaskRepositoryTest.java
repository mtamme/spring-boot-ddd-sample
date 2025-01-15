package com.github.task.management.infrastructure.persistence.task;

import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.task.TaskId;
import com.github.task.management.domain.task.TaskRepository;
import com.github.task.management.domain.task.TaskStatus;
import com.github.task.management.domain.task.Tasks;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaTaskRepositoryTest extends PersistenceTest {

  @Autowired
  private TaskRepository taskRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void saveShouldSaveTask() {
    // Arrange
    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      final var task = Tasks.newOpenTask("00000000000", "00000000001");

      taskRepository.save(task);
    });

    // Assert
    final var task = taskRepository.findByTaskId(new TaskId("00000000001"));

    assertTrue(task.isPresent());
    assertEquals(new ProjectId("00000000000"), task.get().projectId());
    assertEquals(new TaskId("00000000001"), task.get().taskId());
    assertEquals(TaskStatus.OPEN, task.get().status());
    assertEquals(Instant.EPOCH.minusSeconds(3L), task.get().openedAt());
    assertNull(task.get().startedAt());
    assertNull(task.get().closedAt());
    assertEquals("TestSummary", task.get().summary());
    assertEquals("TestDescription", task.get().description());
    assertNull(task.get().assignee());
  }

  @Test
  void saveWithDuplicateTaskIdShouldThrowDataIntegrityViolationException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var task = Tasks.newOpenTask("00000000000", "00000000001");

      taskRepository.save(task);
    });

    // Act
    // Assert
    assertThrows(DataIntegrityViolationException.class, () -> transactionTemplate.executeWithoutResult(ts -> {
      final var task = Tasks.newOpenTask("00000000000", "00000000001");

      taskRepository.save(task);
    }));
  }
}
