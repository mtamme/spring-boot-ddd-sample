package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.infrastructure.lock.EnableLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(prefix = "outbox", value = "enabled", havingValue = "true")
@EnableConfigurationProperties(OutboxProperties.class)
@EnableLock
@EnableScheduling
public class OutboxConfiguration {

  @Bean
  public OutboxEventDispatcher outboxEventDispatcher(final MessageStore messageStore) {
    return new OutboxEventDispatcher(messageStore);
  }

  @Bean
  public OutboxHealthIndicator outboxHealthIndicator(final OutboxProperties properties, final MessageStore messageStore) {
    return new OutboxHealthIndicator(properties, messageStore);
  }

  @Bean
  public OutboxProcessor outboxProcessor(final OutboxProperties properties,
                                         final ApplicationEventPublisher applicationEventPublisher,
                                         final LockRegistry lockRegistry,
                                         final MessageStore messageStore) {
    return new OutboxProcessor(properties, applicationEventPublisher, lockRegistry, messageStore);
  }
}
