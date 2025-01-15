package com.github.seedwork.infrastructure.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "outbox")
public record OutboxProperties(boolean enabled,
                               Duration pollInterval,
                               int maxRequeueCount,
                               int maxPeekCount,
                               Duration initialRequeueDelay) {

  public OutboxProperties(@DefaultValue("false") final boolean enabled,
                          @DefaultValue("PT1S") final Duration pollInterval,
                          @DefaultValue("10") final int maxRequeueCount,
                          @DefaultValue("100") final int maxPeekCount,
                          @DefaultValue("PT5S") final Duration initialRequeueDelay) {
    this.enabled = enabled;
    this.pollInterval = Objects.requireNonNull(pollInterval);
    this.maxRequeueCount = maxRequeueCount;
    this.maxPeekCount = maxPeekCount;
    this.initialRequeueDelay = Objects.requireNonNull(initialRequeueDelay);
  }
}
