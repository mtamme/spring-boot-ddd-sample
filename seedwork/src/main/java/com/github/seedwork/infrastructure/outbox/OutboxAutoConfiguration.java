package com.github.seedwork.infrastructure.outbox;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Objects;

@AutoConfiguration
@ConditionalOnBooleanProperty(prefix = "outbox", name = "enabled")
@EnableConfigurationProperties(OutboxProperties.class)
@EnableScheduling
public class OutboxAutoConfiguration implements SchedulingConfigurer {

  private final OutboxProperties properties;
  private final MessageConsumer messageConsumer;
  private final ApplicationEventPublisher eventPublisher;

  public OutboxAutoConfiguration(final OutboxProperties outboxProperties,
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
  @ConditionalOnClass(HealthIndicator.class)
  @ConditionalOnEnabledHealthIndicator("outbox")
  public OutboxHealthIndicator outboxHealthIndicator(final MessageConsumer messageConsumer) {
    return new OutboxHealthIndicator(messageConsumer);
  }

  @Override
  public void configureTasks(@NonNull final ScheduledTaskRegistrar scheduledTaskRegistrar) {
    if (!properties.isPollingEnabled()) {
      return;
    }
    final var processor = new OutboxProcessor(properties, messageConsumer, eventPublisher);

    scheduledTaskRegistrar.addFixedRateTask(processor, properties.pollInterval());
  }
}
