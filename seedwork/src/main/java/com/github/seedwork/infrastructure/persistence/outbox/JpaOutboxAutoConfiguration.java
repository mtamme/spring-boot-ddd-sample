package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.OutboxAutoConfiguration;
import com.github.seedwork.infrastructure.outbox.OutboxProperties;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import java.time.Clock;

@AutoConfiguration
@AutoConfigureAfter({DataJpaRepositoriesAutoConfiguration.class, OutboxAutoConfiguration.class})
@ConditionalOnBean(OutboxProperties.class)
@ConditionalOnClass(EntityManager.class)
public class JpaOutboxAutoConfiguration {

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

  @Bean
  @ConditionalOnMissingBean(JpaMessageRepository.class)
  public JpaMessageRepository jpaMessageRepository(final EntityManager entityManager) {
    final var repositoryFactory = new JpaRepositoryFactory(entityManager);

    return repositoryFactory.getRepository(JpaMessageRepository.class, new JpaMessageSupportImpl(entityManager));
  }
}
