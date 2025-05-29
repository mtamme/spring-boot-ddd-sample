package com.github.seedwork.infrastructure.event;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DefaultEventPublisherAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(EventPublisher.class)
  public EventPublisher defaultEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
    return new DefaultEventPublisher(applicationEventPublisher);
  }
}
