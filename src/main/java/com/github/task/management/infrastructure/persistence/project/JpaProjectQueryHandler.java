package com.github.task.management.infrastructure.persistence.project;

import com.github.seedwork.infrastructure.persistence.Params;
import com.github.task.management.application.project.ProjectQueryHandler;
import com.github.task.management.application.project.view.ProjectDetailView;
import com.github.task.management.application.project.view.ProjectSummaryView;
import com.github.task.management.domain.project.ProjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JpaProjectQueryHandler implements ProjectQueryHandler {

  private final JpaProjectRepository repository;

  public JpaProjectQueryHandler(final JpaProjectRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public ProjectDetailView getProject(final String projectId) {
    return repository.find(projectId)
      .orElseThrow(ProjectNotFoundException::new);
  }

  @Override
  public List<ProjectSummaryView> listProjects(final long offset, final int limit) {
    return repository.findAll(offset, limit);
  }

  @Override
  public List<ProjectSummaryView> searchProjects(final String query, final long offset, final int limit) {
    return repository.findAllByPattern(Params.containsPattern(query), Params.startsWithPattern(query), offset, limit);
  }
}
