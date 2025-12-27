package com.github.booking.domain.ticket;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class TicketException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("ticket-not-found", "Ticket not found");
  public static final Problem NOT_REDEEMABLE_PROBLEM = Problem.conflict("ticket-not-redeemable", "Ticket not redeemable");

  private TicketException(final Problem problem) {
    super(problem);
  }

  public static TicketException notFound() {
    return new TicketException(NOT_FOUND_PROBLEM);
  }

  public static TicketException notRedeemable() {
    return new TicketException(NOT_REDEEMABLE_PROBLEM);
  }
}
