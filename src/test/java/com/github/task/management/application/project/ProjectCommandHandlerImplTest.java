package com.github.task.management.application.project;

import com.github.task.management.application.project.command.ArchiveProjectCommand;
import com.github.task.management.application.project.command.DefineProjectCommand;
import com.github.task.management.application.project.command.UnarchiveProjectCommand;
import com.github.task.management.domain.project.Project;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.ProjectRepository;
import com.github.task.management.domain.project.Projects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProjectCommandHandlerImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @Test
  void defineProjectShouldDefineProjectAndReturnProjectId() {
    // Arrange
    Mockito.when(projectRepository.nextProjectId())
      .thenReturn(new ProjectId("00000000000"));
    final var projectCaptor = ArgumentCaptor.forClass(Project.class);

    Mockito.doNothing()
      .when(projectRepository)
      .save(projectCaptor.capture());
    final var projectCommandHandler = new ProjectCommandHandlerImpl(projectRepository);
    final var command = new DefineProjectCommand("TestName", "TestDescription");

    // Act
    final var projectId = projectCommandHandler.defineProject(command);

    // Assert
    assertEquals("00000000000", projectId);
    final var project = projectCaptor.getValue();

    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertFalse(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void archiveTaskShouldArchiveTask() {
    // Arrange
    Mockito.when(projectRepository.findByProjectId(new ProjectId("00000000000")))
      .thenReturn(Optional.of(Projects.newProject("00000000000")));
    final var projectCaptor = ArgumentCaptor.forClass(Project.class);

    Mockito.doNothing()
      .when(projectRepository)
      .save(projectCaptor.capture());
    final var projectCommandHandler = new ProjectCommandHandlerImpl(projectRepository);
    final var command = new ArchiveProjectCommand("00000000000");

    // Act
    projectCommandHandler.archiveProject(command);

    // Assert
    final var project = projectCaptor.getValue();

    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertTrue(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }

  @Test
  void unarchiveTaskShouldUnarchiveTask() {
    // Arrange
    Mockito.when(projectRepository.findByProjectId(new ProjectId("00000000000")))
      .thenReturn(Optional.of(Projects.newArchivedProject("00000000000")));
    final var projectCaptor = ArgumentCaptor.forClass(Project.class);

    Mockito.doNothing()
      .when(projectRepository)
      .save(projectCaptor.capture());
    final var projectCommandHandler = new ProjectCommandHandlerImpl(projectRepository);
    final var command = new UnarchiveProjectCommand("00000000000");

    // Act
    projectCommandHandler.unarchiveProject(command);

    // Assert
    final var project = projectCaptor.getValue();

    assertEquals(new ProjectId("00000000000"), project.projectId());
    assertFalse(project.isArchived());
    assertEquals("TestName", project.name());
    assertEquals("TestDescription", project.description());
  }
}
