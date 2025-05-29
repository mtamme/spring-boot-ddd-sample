package com.github.seedwork.infrastructure.event;

import com.github.seedwork.domain.TestEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

class DefaultEventPublisherTest {

  @Test
  void publishEventShouldPublishEvent() {
    // Arrange
    final var applicationEventPublisher = mock(ApplicationEventPublisher.class);

    doNothing()
      .when(applicationEventPublisher)
      .publishEvent(any(Object.class));
    final var defaultEventPublisher = new DefaultEventPublisher(applicationEventPublisher);

    // Act
    defaultEventPublisher.publishEvent("A", new TestEvent());

    // Assert
    verify(applicationEventPublisher, times(1))
      .publishEvent(isA(TestEvent.class));
  }
}
