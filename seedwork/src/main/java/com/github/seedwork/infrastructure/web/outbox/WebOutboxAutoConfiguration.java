package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.MessageConsumer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(MessageConsumer.class)
public class WebOutboxAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(MessageController.class)
  public MessageController messageController(final MessageConsumer messageConsumer, final MessageMapper messageMapper) {
    return new MessageController(messageConsumer, messageMapper);
  }

  @Bean
  public MessageMapper messageMapper() {
    return new MessageMapper();
  }
}
