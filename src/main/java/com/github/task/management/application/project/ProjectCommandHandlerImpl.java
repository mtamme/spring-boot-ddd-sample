package com.github.task.management.application.project;

import com.github.task.management.application.project.command.ArchiveProjectCommand;
import com.github.task.management.application.project.command.DefineProjectCommand;
import com.github.task.management.application.project.command.UnarchiveProjectCommand;
import com.github.task.management.domain.project.Project;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.ProjectNotFoundException;
import com.github.task.management.domain.project.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProjectCommandHandlerImpl implements ProjectCommandHandler {

  private final ProjectRepository projectRepository;

  public ProjectCommandHandlerImpl(final ProjectRepository projectRepository) {
    this.projectRepository = Objects.requireNonNull(projectRepository);
  }

  @Override
  public String defineProject(final DefineProjectCommand command) {
    final var project = new Project(
      projectRepository.nextProjectId(),
      command.name(),
      command.description());

    projectRepository.save(project);

    return project.projectId()
      .value();
  }

  @Override
  public void archiveProject(final ArchiveProjectCommand command) {
    final var project = projectRepository.findByProjectId(new ProjectId(command.projectId()))
      .orElseThrow(ProjectNotFoundException::new);

    project.archive();
    projectRepository.save(project);
  }

  @Override
  public void unarchiveProject(final UnarchiveProjectCommand command) {
    final var project = projectRepository.findByProjectId(new ProjectId(command.projectId()))
      .orElseThrow(ProjectNotFoundException::new);

    project.unarchive();
    projectRepository.save(project);
  }
}
