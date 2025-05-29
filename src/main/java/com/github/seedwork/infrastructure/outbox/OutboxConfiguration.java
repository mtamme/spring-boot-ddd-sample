package com.github.seedwork.infrastructure.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Objects;

@Configuration
@ConditionalOnProperty(prefix = "outbox", value = "enabled", havingValue = "true")
@EnableConfigurationProperties(OutboxProperties.class)
@EnableScheduling
public class OutboxConfiguration implements SchedulingConfigurer {

  private final OutboxProperties properties;
  private final MessageConsumer messageConsumer;
  private final ApplicationEventPublisher eventPublisher;

  public OutboxConfiguration(final OutboxProperties outboxProperties,
                             final MessageConsumer messageConsumer,
                             final ApplicationEventPublisher eventPublisher) {
    this.properties = Objects.requireNonNull(outboxProperties);
    this.messageConsumer = Objects.requireNonNull(messageConsumer);
    this.eventPublisher = Objects.requireNonNull(eventPublisher);
  }

  @Bean
  public OutboxEventPublisher outboxEventPublisher(final MessageProducer messageProducer) {
    return new OutboxEventPublisher(messageProducer);
  }

  @Bean
  public OutboxHealthIndicator outboxHealthIndicator(final MessageConsumer messageConsumer) {
    return new OutboxHealthIndicator(messageConsumer);
  }

  @Override
  public void configureTasks(final ScheduledTaskRegistrar scheduledTaskRegistrar) {
    if (!properties.isPolling()) {
      return;
    }
    final var processor = new OutboxProcessor(properties, messageConsumer, eventPublisher);

    scheduledTaskRegistrar.addFixedRateTask(processor, properties.pollInterval());
  }
}
