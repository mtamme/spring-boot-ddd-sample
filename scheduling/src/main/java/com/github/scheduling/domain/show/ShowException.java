package com.github.scheduling.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class ShowException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("show-not-found", "Show not found");
  public static final Problem OVERLAP_PROBLEM = Problem.conflict("show-overlap", "Hall already has a show at this time");
  public static final Problem PAST_SCHEDULE_PROBLEM = Problem.conflict("show-past-schedule", "Cannot schedule a show in the past");

  private ShowException(final Problem problem) {
    super(problem);
  }

  public static ShowException notFound() {
    return new ShowException(NOT_FOUND_PROBLEM);
  }

  public static ShowException overlap() {
    return new ShowException(OVERLAP_PROBLEM);
  }

  public static ShowException pastSchedule() {
    return new ShowException(PAST_SCHEDULE_PROBLEM);
  }
}
