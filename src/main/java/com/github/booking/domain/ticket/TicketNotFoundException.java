package com.github.booking.domain.ticket;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class TicketNotFoundException extends ProblemException {

  private static final Problem PROBLEM = Problem.notFound("ticket-not-found", "Ticket not found");

  public TicketNotFoundException() {
    super(PROBLEM);
  }
}
