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
                               @DefaultValue("10") @Min(0) int maxRequeueCount,
                               @DefaultValue("100") @Min(1) int maxPeekCount,
                               @DefaultValue("PT5S") Duration initialRequeueDelay) {

  public boolean isPolling() {
    return pollInterval().isPositive();
  }
}
