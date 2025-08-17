package com.github.booking.domain.ticket;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class TicketNotRedeemableException extends ProblemException {

  private static final Problem PROBLEM = Problem.conflict("ticket-not-redeemable", "Ticket not redeemable");

  public TicketNotRedeemableException() {
    super(PROBLEM);
  }
}
