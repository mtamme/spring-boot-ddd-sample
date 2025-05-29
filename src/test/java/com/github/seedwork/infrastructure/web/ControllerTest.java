package com.github.seedwork.infrastructure.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ControllerTest {

  @LocalServerPort
  private int port;

  private String baseUrl() {
    return "http://localhost:%d".formatted(port);
  }

  public RestClient client() {
    return RestClient.builder()
      .baseUrl(baseUrl())
      .build();
  }
}
