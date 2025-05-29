package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.domain.TestEvent;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public final class Messages {

  private Messages() {
  }

  public static Message newMessage(final String groupId,
                                   final Instant enqueuedAt,
                                   final Instant availableAt,
                                   final UUID lockId,
                                   final int deliveryCount) {
    return new Message(
      null,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      deliveryCount,
      TestEvent.class.getSimpleName(),
      new TestEvent());
  }

  public static Message newMessage(final long sequenceNumber,
                                   final String groupId,
                                   final Instant enqueuedAt,
                                   final Instant availableAt,
                                   final UUID lockId,
                                   final int deliveryCount) {
    return new Message(
      sequenceNumber,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      deliveryCount,
      TestEvent.class.getSimpleName(),
      new TestEvent());
  }

  public static Message newMessage(final long sequenceNumber,
                                   final String groupId,
                                   final Instant enqueuedAt,
                                   final Instant availableAt,
                                   final UUID lockId,
                                   final int deliveryCount,
                                   final String subject,
                                   final Serializable body) {
    return new Message(
      sequenceNumber,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      deliveryCount,
      subject,
      body);
  }

  public static Message newUnprocessableMessage(final long sequenceNumber,
                                                final String groupId,
                                                final Instant enqueuedAt,
                                                final Instant availableAt,
                                                final UUID lockId,
                                                final int deliveryCount) {
    return new Message(
      sequenceNumber,
      groupId,
      enqueuedAt,
      availableAt,
      lockId,
      deliveryCount,
      TestEvent.class.getSimpleName(),
      new TestEvent(false));
  }
}
