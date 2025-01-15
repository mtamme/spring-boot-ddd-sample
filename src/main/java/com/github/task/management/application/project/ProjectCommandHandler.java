package com.github.task.management.application.project;

import com.github.task.management.application.project.command.ArchiveProjectCommand;
import com.github.task.management.application.project.command.DefineProjectCommand;
import com.github.task.management.application.project.command.UnarchiveProjectCommand;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProjectCommandHandler {

  String defineProject(DefineProjectCommand command);

  void archiveProject(ArchiveProjectCommand command);

  void unarchiveProject(UnarchiveProjectCommand command);
}
