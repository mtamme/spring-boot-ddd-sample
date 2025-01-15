package com.github.task.management.infrastructure.persistence.project;

import com.github.seedwork.core.util.Longs;
import com.github.seedwork.infrastructure.persistence.JpaAggregateRepository;
import com.github.task.management.application.project.view.ProjectDetailView;
import com.github.task.management.application.project.view.ProjectSummaryView;
import com.github.task.management.domain.project.Project;
import com.github.task.management.domain.project.ProjectId;
import com.github.task.management.domain.project.ProjectRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaProjectRepository extends Repository<Project, Long>, JpaAggregateRepository<Project>, ProjectRepository {

  @NativeQuery(name = "ProjectDetailView.find")
  Optional<ProjectDetailView> find(@Param("project_id") String projectId);

  @NativeQuery(name = "ProjectSummaryView.findAll")
  List<ProjectSummaryView> findAll(@Param("offset") long offset,
                                   @Param("limit") int limit);

  @NativeQuery(name = "ProjectSummaryView.findAllByPattern")
  List<ProjectSummaryView> findAllByPattern(@Param("filter_pattern") String filterPattern,
                                            @Param("sort_pattern") String sortPattern,
                                            @Param("offset") long offset,
                                            @Param("limit") int limit);

  @Override
  default ProjectId nextProjectId() {
    return new ProjectId(Longs.toBase64String(Longs.nextLong()));
  }
}
