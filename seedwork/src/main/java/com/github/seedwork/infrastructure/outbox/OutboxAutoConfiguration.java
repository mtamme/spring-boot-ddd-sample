package com.github.seedwork.infrastructure.outbox;

import com.github.seedwork.infrastructure.event.DefaultEventPublisherAutoConfiguration;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
@AutoConfigureBefore(DefaultEventPublisherAutoConfiguration.class)
@ConditionalOnBooleanProperty(prefix = "outbox", name = "enabled")
@EnableConfigurationProperties(OutboxProperties.class)
@EnableScheduling
public class OutboxAutoConfiguration implements SchedulingConfigurer {

  private final OutboxProperties properties;
  private final MessageConsumer messageConsumer;
  private final ApplicationEventPublisher applicationEventPublisher;

  public OutboxAutoConfiguration(final OutboxProperties outboxProperties,
                                 final MessageConsumer messageConsumer,
                                 final ApplicationEventPublisher applicationEventPublisher) {
    this.properties = Objects.requireNonNull(outboxProperties);
    this.messageConsumer = Objects.requireNonNull(messageConsumer);
    this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
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
    if (!properties.isPollerEnabled()) {
      return;
    }
    final var poller = new OutboxPoller(properties, messageConsumer, applicationEventPublisher);

    scheduledTaskRegistrar.addTriggerTask(poller, poller.trigger());
  }
}
