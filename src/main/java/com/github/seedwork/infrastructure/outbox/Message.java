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
    setMessageId(messageId);
    setCorrelationId(correlationId);
    setType(payload);
    setEnqueuedAt(enqueuedAt);
    setScheduledAt(scheduledAt);
    setRequeueCount(requeueCount);
    setPayload(payload);
  }

  public UUID messageId() {
    return messageId;
  }

  private void setMessageId(final UUID messageId) {
    this.messageId = Objects.requireNonNull(messageId);
  }

  public String correlationId() {
    return correlationId;
  }

  private void setCorrelationId(final String correlationId) {
    this.correlationId = Objects.requireNonNull(correlationId);
  }

  public String type() {
    return type;
  }

  private void setType(final Serializable payload) {
    this.type = payload.getClass().getSimpleName();
  }

  public Instant enqueuedAt() {
    return enqueuedAt;
  }

  private void setEnqueuedAt(final Instant enqueuedAt) {
    this.enqueuedAt = Objects.requireNonNull(enqueuedAt);
  }

  public Instant scheduledAt() {
    return scheduledAt;
  }

  private void setScheduledAt(final Instant scheduledAt) {
    this.scheduledAt = Objects.requireNonNull(scheduledAt);
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

  private void setRequeueCount(final int requeueCount) {
    this.requeueCount = requeueCount;
  }

  public int nextRequeueCount() {
    return requeueCount() + 1;
  }

  public Serializable payload() {
    return payload;
  }

  private void setPayload(final Serializable payload) {
    this.payload = Objects.requireNonNull(payload);
  }

  protected Message() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof Message other)) {
      return false;
    }

    return Objects.equals(other.messageId(), messageId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(messageId());
  }
}
