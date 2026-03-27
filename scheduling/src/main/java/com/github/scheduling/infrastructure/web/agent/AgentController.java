package com.github.scheduling.infrastructure.web.agent;

import com.github.scheduling.infrastructure.agent.show.ShowSchedulingAgent;
import com.github.scheduling.infrastructure.web.AgentOperations;
import com.github.scheduling.infrastructure.web.representation.AgentMessageRequest;
import com.github.scheduling.infrastructure.web.representation.AgentMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController implements AgentOperations {

  private final ShowSchedulingAgent showSchedulingAgent;

  public AgentController(final ShowSchedulingAgent showSchedulingAgent) {
    this.showSchedulingAgent = showSchedulingAgent;
  }

  @Override
  public ResponseEntity<AgentMessageResponse> sendAgentMessage(final AgentMessageRequest request) {
    final var response = showSchedulingAgent.processMessage(request.getMessage());

    return ResponseEntity.status(HttpStatus.CREATED).body(new AgentMessageResponse().response(response));
  }
}
