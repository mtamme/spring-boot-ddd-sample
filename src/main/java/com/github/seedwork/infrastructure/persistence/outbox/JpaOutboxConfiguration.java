package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.OutboxConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@ConditionalOnBean(OutboxConfiguration.class)
public class JpaOutboxConfiguration {

  @Bean
  public JpaMessageStore jpaMessageStore(final Clock clock, final JpaMessageRepository messageRepository) {
    return new JpaMessageStore(clock, messageRepository);
  }
}
