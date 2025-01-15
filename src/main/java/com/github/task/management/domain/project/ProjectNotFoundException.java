package com.github.task.management.domain.project;

import com.github.seedwork.core.problem.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

  public ProjectNotFoundException() {
    super("Project not found");
  }
}
