package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class MessageNotFoundException extends ProblemException {

  private static final Problem PROBLEM = Problem.notFound("message-not-found", "Message not found");

  public MessageNotFoundException() {
    super(PROBLEM);
  }
}
