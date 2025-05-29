package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.core.problem.Problem;
import com.github.seedwork.core.problem.ProblemException;

public class MessageException extends ProblemException {

  public static final Problem NOT_FOUND_PROBLEM = Problem.notFound("message-not-found", "Message not found");

  private MessageException(final Problem problem) {
    super(problem);
  }

  public static MessageException notFound() {
    return new MessageException(NOT_FOUND_PROBLEM);
  }
}
