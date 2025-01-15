package com.github.task.management.application.project;

import com.github.task.management.application.project.view.ProjectDetailView;
import com.github.task.management.application.project.view.ProjectSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ProjectQueryHandler {

  ProjectDetailView getProject(String projectId);

  List<ProjectSummaryView> listProjects(long offset, int limit);

  List<ProjectSummaryView> searchProjects(String query, long offset, int limit);
}
