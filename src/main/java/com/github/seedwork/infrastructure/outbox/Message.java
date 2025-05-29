package com.github.seedwork.infrastructure.outbox;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Message {

  private UUID messageId;
  private String correlationId;
  private String type;
  private Instant enqueuedAt;
  private Instant scheduledAt;
  private int requeueCount;
  private Serializable payload;

  public Message(final UUID messageId,
                 final String correlationId,
                 final Instant enqueuedAt,
                 final Serializable payload) {
    this(messageId, correlationId, enqueuedAt, enqueuedAt, 0, payload);
  }

  public Message(final UUID messageId,
                 final String correlationId,
                 final Instant enqueuedAt,
                 final Instant scheduledAt,
                 final int requeueCount,
                 final Serializable payload) {
    this.messageId = Objects.requireNonNull(messageId);
    this.correlationId = Objects.requireNonNull(correlationId);
    this.type = payload.getClass().getSimpleName();
    this.enqueuedAt = Objects.requireNonNull(enqueuedAt);
    this.scheduledAt = Objects.requireNonNull(scheduledAt);
    this.requeueCount = requeueCount;
    this.payload = Objects.requireNonNull(payload);
  }

  public UUID messageId() {
    return messageId;
  }

  public String correlationId() {
    return correlationId;
  }

  public String type() {
    return type;
  }

  public Instant enqueuedAt() {
    return enqueuedAt;
  }

  public Instant scheduledAt() {
    return scheduledAt;
  }

  public Instant nextScheduledAt(final Duration requeueDelay) {
    return scheduledAt().plus(requeueDelay);
  }

  public Duration delay() {
    return Duration.between(enqueuedAt(), scheduledAt());
  }

  public int requeueCount() {
    return requeueCount;
  }

  public int nextRequeueCount() {
    return requeueCount() + 1;
  }

  public Serializable payload() {
    return payload;
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

    return Objects.equals(other.messageId(), messageId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(messageId());
  }
}
