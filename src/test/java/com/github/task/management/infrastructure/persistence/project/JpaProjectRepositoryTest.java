package com.github.task.management.infrastructure.persistence.project;

import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.ProjectRepository;
import com.github.task.management.domain.project.Projects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

class JpaProjectRepositoryTest extends PersistenceTest {

  @Autowired
  private ProjectRepository projectRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void saveShouldSaveProject() {
    // Arrange
    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      final var project = Projects.newProject("00000000000");

      projectRepository.save(project);
    });

    // Assert
    final var project = projectRepository.findByProjectId(new ProjectId("00000000000"));

    assertTrue(project.isPresent());
    assertEquals(new ProjectId("00000000000"), project.get().projectId());
    assertFalse(project.get().isArchived());
    assertEquals("TestName", project.get().name());
    assertEquals("TestDescription", project.get().description());
  }

  @Test
  void saveWithDuplicateProjectIdShouldThrowDataIntegrityViolationException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var project = Projects.newProject("00000000000");

      projectRepository.save(project);
    });

    // Act
    // Assert
    assertThrows(DataIntegrityViolationException.class, () -> transactionTemplate.executeWithoutResult(ts -> {
      final var project = Projects.newProject("00000000000");

      projectRepository.save(project);
    }));
  }
}
