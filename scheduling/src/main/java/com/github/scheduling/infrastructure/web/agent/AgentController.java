package com.github.scheduling.infrastructure.web.agent;

import com.github.scheduling.infrastructure.agent.SchedulingAgent;
import com.github.scheduling.infrastructure.web.AgentOperations;
import com.github.scheduling.infrastructure.web.representation.AgentMessageRequest;
import com.github.scheduling.infrastructure.web.representation.AgentMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController implements AgentOperations {

  private final SchedulingAgent schedulingAgent;

  public AgentController(final SchedulingAgent schedulingAgent) {
    this.schedulingAgent = schedulingAgent;
  }

  @Override
  public ResponseEntity<AgentMessageResponse> sendAgentMessage(final AgentMessageRequest request) {
    final var response = schedulingAgent.processMessage(request.getMessage());

    return ResponseEntity.status(HttpStatus.CREATED).body(new AgentMessageResponse().response(response));
  }
}
