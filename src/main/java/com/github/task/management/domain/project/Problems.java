package com.github.task.management.domain.project;

import com.github.seedwork.core.problem.Problem;

public final class Problems {

  public static final Problem PROJECT_NOT_ARCHIVABLE = new Problem("project-not-archivable", "Project not archivable");
  public static final Problem PROJECT_NOT_UNARCHIVABLE = new Problem("project-not-unarchivable", "Project not unarchivable");

  private Problems() {
  }
}
