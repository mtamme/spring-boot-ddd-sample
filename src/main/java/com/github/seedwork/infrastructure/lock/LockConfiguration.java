package com.github.seedwork.infrastructure.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.support.locks.LockRegistry;

import javax.sql.DataSource;

public class LockConfiguration {

  @Bean
  public LockRepository lockRepository(final DataSource dataSource) {
    return new DefaultLockRepository(dataSource);
  }

  @Bean
  public LockRegistry lockRegistry(final LockRepository lockRepository) {
    return new JdbcLockRegistry(lockRepository);
  }
}
