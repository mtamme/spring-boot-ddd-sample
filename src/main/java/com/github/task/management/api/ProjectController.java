package com.github.task.management.api;

import com.github.task.management.api.representation.DefineProjectRequest;
import com.github.task.management.api.representation.DefineProjectResponse;
import com.github.task.management.api.representation.GetProjectResponse;
import com.github.task.management.api.representation.ListProjectsResponse;
import com.github.task.management.api.representation.ProjectSummary;
import com.github.task.management.api.representation.SearchProjectsResponse;
import com.github.task.management.application.project.ProjectCommandHandler;
import com.github.task.management.application.project.ProjectQueryHandler;
import com.github.task.management.application.project.command.ArchiveProjectCommand;
import com.github.task.management.application.project.command.DefineProjectCommand;
import com.github.task.management.application.project.command.UnarchiveProjectCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ProjectController implements ProjectOperations {

  private final ProjectCommandHandler projectCommandHandler;
  private final ProjectQueryHandler projectQueryHandler;

  public ProjectController(final ProjectCommandHandler projectCommandHandler, final ProjectQueryHandler projectQueryHandler) {
    this.projectCommandHandler = Objects.requireNonNull(projectCommandHandler);
    this.projectQueryHandler = Objects.requireNonNull(projectQueryHandler);
  }

  @Override
  public ResponseEntity<GetProjectResponse> getProject(final String projectId) {
    final var project = projectQueryHandler.getProject(projectId);
    final var response = new GetProjectResponse()
      .projectId(project.projectId())
      .archived(project.archived())
      .name(project.name())
      .description(project.description());

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ListProjectsResponse> listProjects(final Long offset, final Integer limit) {
    final var projects = projectQueryHandler.listProjects(offset, limit)
      .stream()
      .map(p -> new ProjectSummary()
        .projectId(p.projectId())
        .archived(p.archived())
        .name(p.name()))
      .toList();
    final var response = new ListProjectsResponse()
      .projects(projects);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<DefineProjectResponse> defineProject(final DefineProjectRequest request) {
    final var projectId = projectCommandHandler.defineProject(new DefineProjectCommand(request.getName(), request.getDescription()));
    final var response = new DefineProjectResponse()
      .projectId(projectId);

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(response);
  }

  @Override
  public ResponseEntity<Void> archiveProject(final String projectId) {
    projectCommandHandler.archiveProject(new ArchiveProjectCommand(projectId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> unarchiveProject(final String projectId) {
    projectCommandHandler.unarchiveProject(new UnarchiveProjectCommand(projectId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<SearchProjectsResponse> searchProjects(final String query, final Long offset, final Integer limit) {
    final var projects = projectQueryHandler.searchProjects(query, offset, limit)
      .stream()
      .map(p -> new ProjectSummary()
        .projectId(p.projectId())
        .archived(p.archived())
        .name(p.name()))
      .toList();
    final var response = new SearchProjectsResponse()
      .projects(projects);

    return ResponseEntity.ok(response);
  }
}
