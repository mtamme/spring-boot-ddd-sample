package com.github.task.management.api;

import com.github.seedwork.api.ControllerTest;
import com.github.task.management.api.representation.DefineProjectRequest;
import com.github.task.management.api.representation.DefineProjectResponse;
import com.github.task.management.api.representation.GetProjectResponse;
import com.github.task.management.api.representation.ListProjectsResponse;
import com.github.task.management.api.representation.SearchProjectsResponse;
import com.github.task.management.application.project.ProjectCommandHandler;
import com.github.task.management.application.project.ProjectQueryHandler;
import com.github.task.management.application.project.command.ArchiveProjectCommand;
import com.github.task.management.application.project.command.DefineProjectCommand;
import com.github.task.management.application.project.command.UnarchiveProjectCommand;
import com.github.task.management.application.project.view.ProjectDetailView;
import com.github.task.management.application.project.view.ProjectSummaryView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectControllerTest extends ControllerTest {

  @MockitoBean
  private ProjectCommandHandler projectCommandHandler;
  @MockitoBean
  private ProjectQueryHandler projectQueryHandler;

  @Test
  void defineProjectShouldReturnDefineProjectResponse() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(DefineProjectCommand.class);

    Mockito.when(projectCommandHandler.defineProject(commandCaptor.capture()))
      .thenReturn("00000000000");

    // Act
    final var entity = client().post()
      .uri("/projects")
      .body(new DefineProjectRequest("TestName", "TestDescription"))
      .retrieve()
      .toEntity(DefineProjectResponse.class);

    // Assert
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("00000000000", response.getProjectId());
    final var command = commandCaptor.getValue();

    assertEquals("TestName", command.name());
    assertEquals("TestDescription", command.description());
  }

  @Test
  void archiveProjectShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ArchiveProjectCommand.class);

    Mockito.doNothing()
      .when(projectCommandHandler)
      .archiveProject(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/archived-projects/{project_id}", "00000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("00000000000", command.projectId());
  }

  @Test
  void unarchiveProjectShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(UnarchiveProjectCommand.class);

    Mockito.doNothing()
      .when(projectCommandHandler)
      .unarchiveProject(commandCaptor.capture());

    // Act
    final var entity = client().delete()
      .uri("/archived-projects/{project_id}", "00000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("00000000000", command.projectId());
  }

  @Test
  void getProjectShouldReturnGetProjectResponse() {
    // Arrange
    Mockito.when(projectQueryHandler.getProject("00000000000"))
      .thenReturn(new ProjectDetailView(
        "00000000000",
        false,
        "TestName",
        "TestDescription"));

    // Act
    final var entity = client().get()
      .uri("/projects/{project_id}", "00000000000")
      .retrieve()
      .toEntity(GetProjectResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("00000000000", response.getProjectId());
    assertFalse(response.getArchived());
    assertEquals("TestName", response.getName());
    assertEquals("TestDescription", response.getDescription());
  }

  @Test
  void listProjectsShouldReturnListProjectsResponse() {
    // Arrange
    Mockito.when(projectQueryHandler.listProjects(0, 10))
      .thenReturn(List.of(new ProjectSummaryView(
        "00000000000",
        false,
        "TestName")));

    // Act
    final var entity = client().get()
      .uri("/projects")
      .retrieve()
      .toEntity(ListProjectsResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var projects = response.getProjects();

    assertEquals(1, projects.size());
    final var project = projects.getFirst();

    assertEquals("00000000000", project.getProjectId());
    assertFalse(project.getArchived());
    assertEquals("TestName", project.getName());
  }

  @Test
  void searchProjectsShouldReturnSearchProjectsResponse() {
    // Arrange
    Mockito.when(projectQueryHandler.searchProjects("test name", 0, 10))
      .thenReturn(List.of(new ProjectSummaryView(
        "00000000000",
        false,
        "TestName")));

    // Act
    final var entity = client().get()
      .uri("/search/projects?q={query}", "test name")
      .retrieve()
      .toEntity(SearchProjectsResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var projects = response.getProjects();

    assertEquals(1, projects.size());
    final var project = projects.getFirst();

    assertEquals("00000000000", project.getProjectId());
    assertFalse(project.getArchived());
    assertEquals("TestName", project.getName());
  }
}
