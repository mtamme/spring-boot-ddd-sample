package com.github.task.management.infrastructure.persistence.project;

import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import com.github.task.management.application.project.ProjectQueryHandler;
import com.github.task.management.domain.project.ProjectRepository;
import com.github.task.management.domain.project.Projects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

class JpaProjectQueryHandlerTest extends PersistenceTest {

  @Autowired
  private ProjectRepository projectRepository;
  @Autowired
  private ProjectQueryHandler projectQueryHandler;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void getProjectShouldReturnProject() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var project = Projects.newProject("00000000000");

      projectRepository.save(project);
    });

    // Act
    final var project = projectQueryHandler.getProject("00000000000");

    // Assert
    assertEquals("00000000000", project.projectId());
    assertFalse(project.archived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void listProjectsShouldReturnProjects() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var project = Projects.newProject("00000000000");

      projectRepository.save(project);
    });

    // Act
    final var projects = projectQueryHandler.listProjects(0L, 1);

    // Assert
    final var project = projects.getFirst();

    assertEquals("00000000000", project.projectId());
    assertFalse(project.archived());
    assertEquals("TestName", project.name());
  }

  @Test
  void searchProjectsShouldReturnProjects() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var project = Projects.newProject("00000000000");

      projectRepository.save(project);
    });

    // Act
    final var projects = projectQueryHandler.searchProjects("test name", 0L, 1);

    // Assert
    final var project = projects.getFirst();

    assertEquals("00000000000", project.projectId());
    assertFalse(project.archived());
    assertEquals("TestName", project.name());
  }
}
