package com.github.seedwork.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class PersistenceTest {

  @BeforeEach
  void clearDatabase(@Autowired Flyway flyway) {
    flyway.clean();
    flyway.migrate();
  }
}
