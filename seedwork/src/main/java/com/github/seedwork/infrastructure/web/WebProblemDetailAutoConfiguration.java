package com.github.seedwork.infrastructure.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebProblemDetailAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(ProblemDetailExceptionHandler.class)
  public ProblemDetailExceptionHandler problemDetailExceptionHandler() {
    return new ProblemDetailExceptionHandler();
  }
}
