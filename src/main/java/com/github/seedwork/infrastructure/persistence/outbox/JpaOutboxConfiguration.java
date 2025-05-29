package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.OutboxConfiguration;
import com.github.seedwork.infrastructure.outbox.OutboxProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@ConditionalOnBean(OutboxConfiguration.class)
public class JpaOutboxConfiguration {

  @Bean
  public JpaMessageConsumer jpaMessageConsumer(final OutboxProperties properties,
                                               final JpaMessageRepository messageRepository,
                                               final Clock clock) {
    return new JpaMessageConsumer(messageRepository, clock, properties.lockDuration(), properties.maxAttemptCount());
  }

  @Bean
  public JpaMessageProducer jpaMessageProducer(final JpaMessageRepository messageRepository,
                                               final Clock clock) {
    return new JpaMessageProducer(messageRepository, clock);
  }
}
