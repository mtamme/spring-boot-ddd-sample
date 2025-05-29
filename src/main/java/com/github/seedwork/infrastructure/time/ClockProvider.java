package com.github.seedwork.infrastructure.time;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ClockProvider {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
