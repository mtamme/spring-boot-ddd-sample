package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.core.problem.NotFoundException;

public class MessageNotFoundException extends NotFoundException {

  public MessageNotFoundException() {
    super("Message not found");
  }
}
