package com.github.seedwork.infrastructure.outbox;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "outbox")
@Validated
public record OutboxProperties(@DefaultValue("false") boolean enabled,
                               @DefaultValue("PT1S") Duration pollInterval,
                               @DefaultValue("PT30S") Duration lockDuration,
                               @DefaultValue("100") @Min(1) int lockLimit,
                               @DefaultValue("10") @Min(0) int maxAttemptCount) {

  public boolean isPolling() {
    return pollInterval().isPositive();
  }
}
