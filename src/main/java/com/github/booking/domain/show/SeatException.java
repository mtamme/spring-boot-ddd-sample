package com.github.booking.domain.show;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class SeatException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("seat-not-found", "Seat not found");
  public static final Problem NOT_BOOKABLE_PROBLEM = Problem.conflict("seat-not-bookable", "Seat not bookable");
  public static final Problem NOT_RELEASEABLE_PROBLEM = Problem.conflict("seat-not-releasable", "Seat not releasable");
  public static final Problem NOT_RESERVABLE_PROBLEM = Problem.conflict("seat-not-reservable", "Seat not reservable");

  private SeatException(final Problem problem) {
    super(problem);
  }

  public static SeatException notFound() {
    return new SeatException(NOT_FOUND_PROBLEM);
  }

  public static SeatException notBookable() {
    return new SeatException(NOT_BOOKABLE_PROBLEM);
  }

  public static SeatException notReleasable() {
    return new SeatException(NOT_RELEASEABLE_PROBLEM);
  }

  public static SeatException notReservable() {
    return new SeatException(NOT_RESERVABLE_PROBLEM);
  }
}
