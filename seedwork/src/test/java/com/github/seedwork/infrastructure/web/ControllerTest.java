package com.github.seedwork.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class ControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  public MockMvc mockMvc() {
    return MockMvcBuilders.webAppContextSetup(webApplicationContext)
      .build();
  }
}
