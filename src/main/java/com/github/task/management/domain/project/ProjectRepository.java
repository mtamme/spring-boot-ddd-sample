package com.github.task.management.domain.project;

import java.util.Optional;

public interface ProjectRepository {

  ProjectId nextProjectId();

  Optional<Project> findByProjectId(ProjectId projectId);

  void save(Project project);
}
