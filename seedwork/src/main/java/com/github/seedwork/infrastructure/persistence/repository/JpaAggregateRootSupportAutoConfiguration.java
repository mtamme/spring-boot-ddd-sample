package com.github.seedwork.infrastructure.persistence.repository;

import com.github.seedwork.infrastructure.event.DefaultEventPublisher;
import com.github.seedwork.infrastructure.event.EventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class JpaAggregateRootSupportAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(EventPublisher.class)
  public EventPublisher defaultEventPublisher(final ApplicationEventPublisher eventPublisher) {
    return new DefaultEventPublisher(eventPublisher);
  }
}
