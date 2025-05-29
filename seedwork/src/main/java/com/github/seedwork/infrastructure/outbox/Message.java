package com.github.seedwork.infrastructure.outbox;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Message {

  private Long sequenceNumber;
  private String groupId;
  private Instant enqueuedAt;
  private Instant availableAt;
  private UUID lockId;
  private int attemptCount;
  private String subject;
  private Serializable body;

  public Message(final String groupId,
                 final Instant enqueuedAt,
                 final String subject,
                 final Serializable body) {
    this(null, groupId, enqueuedAt, enqueuedAt, null, 0, subject, body);
  }

  public Message(final Long sequenceNumber,
                 final String groupId,
                 final Instant enqueuedAt,
                 final Instant availableAt,
                 final UUID lockId,
                 final int attemptCount,
                 final String subject,
                 final Serializable body) {
    this.sequenceNumber = sequenceNumber;
    this.groupId = Objects.requireNonNull(groupId);
    this.enqueuedAt = Objects.requireNonNull(enqueuedAt);
    this.availableAt = Objects.requireNonNull(availableAt);
    this.lockId = lockId;
    this.attemptCount = attemptCount;
    this.subject = Objects.requireNonNull(subject);
    this.body = Objects.requireNonNull(body);
  }

  public Long sequenceNumber() {
    return sequenceNumber;
  }

  public String groupId() {
    return groupId;
  }

  public Instant enqueuedAt() {
    return enqueuedAt;
  }

  public Instant availableAt() {
    return availableAt;
  }

  public UUID lockId() {
    return lockId;
  }

  public int attemptCount() {
    return attemptCount;
  }

  public String subject() {
    return subject;
  }

  public Serializable body() {
    return body;
  }

  protected Message() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof final Message other)) {
      return false;
    }

    return Objects.equals(other.sequenceNumber(), sequenceNumber());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(sequenceNumber());
  }
}
