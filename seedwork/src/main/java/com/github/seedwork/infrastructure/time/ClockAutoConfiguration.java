package com.github.seedwork.infrastructure.time;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@AutoConfiguration
public class ClockAutoConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
