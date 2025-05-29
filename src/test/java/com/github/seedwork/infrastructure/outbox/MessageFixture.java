package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.TestEvent;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public final class MessageFixture {

  private MessageFixture() {
  }

  public static Message newMessage(final String groupId,
                                   final Instant enqueuedAt,
                                   final Instant availableAt,
                                   final UUID lockId,
                                   final int attemptCount) {
    return new Message(
      null,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      attemptCount,
      TestEvent.class.getSimpleName(),
      new TestEvent());
  }

  public static Message newMessage(final long sequenceNumber,
                                   final String groupId,
                                   final Instant enqueuedAt,
                                   final Instant availableAt,
                                   final UUID lockId,
                                   final int attemptCount) {
    return new Message(
      sequenceNumber,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      attemptCount,
      TestEvent.class.getSimpleName(),
      new TestEvent());
  }

  public static Message newMessage(final long sequenceNumber,
                                   final String groupId,
                                   final Instant enqueuedAt,
                                   final Instant availableAt,
                                   final UUID lockId,
                                   final int attemptCount,
                                   final String subject,
                                   final Serializable body) {
    return new Message(
      sequenceNumber,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      attemptCount,
      subject,
      body);
  }

  public static Message newUnprocessableMessage(final long sequenceNumber,
                                                final String groupId,
                                                final Instant enqueuedAt,
                                                final Instant availableAt,
                                                final UUID lockId,
                                                final int attemptCount) {
    return new Message(
      sequenceNumber,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      attemptCount,
      TestEvent.class.getSimpleName(),
      new TestEvent(false));
  }
}
