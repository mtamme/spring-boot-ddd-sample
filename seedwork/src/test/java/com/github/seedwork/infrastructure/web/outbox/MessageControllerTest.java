package com.github.seedwork.infrastructure.web.outbox;

import com.github.seedwork.infrastructure.outbox.MessageConsumer;
import com.github.seedwork.infrastructure.outbox.MessageFixture;
import com.github.seedwork.infrastructure.web.ControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MessageControllerTest extends ControllerTest {

  @MockitoBean
  private MessageConsumer messageConsumer;

  @Test
  void peekMessagesShouldReturnPeekMessagesResponse() throws Exception {
    // Arrange
    when(messageConsumer.peekAll(eq(0L), eq(10)))
      .thenReturn(List.of(MessageFixture.newMessage(
        0L,
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0)));

    // Act
    // Assert
    mockMvc().perform(get("/outbox/messages")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.messages.length()").value(1))
      .andExpect(jsonPath("$.messages[0].sequenceNumber").value(0))
      .andExpect(jsonPath("$.messages[0].groupId").value("A"))
      .andExpect(jsonPath("$.messages[0].enqueuedAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.messages[0].availableAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.messages[0].attemptCount").value(0))
      .andExpect(jsonPath("$.messages[0].subject").value("TestEvent"));
  }

  @Test
  void peekMessageShouldReturnPeekMessageResponse() throws Exception {
    // Arrange
    when(messageConsumer.peek(eq(0L)))
      .thenReturn(MessageFixture.newMessage(
        0L,
        "A",
        Instant.EPOCH,
        Instant.EPOCH,
        null,
        0));

    // Act
    // Assert
    mockMvc().perform(get("/outbox/messages/{sequence_number}", 0L)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.sequenceNumber").value(0))
      .andExpect(jsonPath("$.groupId").value("A"))
      .andExpect(jsonPath("$.enqueuedAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.availableAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.attemptCount").value(0))
      .andExpect(jsonPath("$.subject").value("TestEvent"));
  }

  @Test
  void lockNextMessagesShouldReturnLockNextMessagesResponse() throws Exception {
    // Arrange
    when(messageConsumer.lockAllNextFailed(any(UUID.class), eq(10)))
      .thenReturn(List.of(MessageFixture.newMessage(
        0L,
        "A",
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(30L),
        UUID.randomUUID(),
        10)));

    // Act
    // Assert
    mockMvc().perform(post("/outbox/messages/locks")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.lockId").isString())
      .andExpect(jsonPath("$.messages.length()").value(1))
      .andExpect(jsonPath("$.messages[0].sequenceNumber").value(0))
      .andExpect(jsonPath("$.messages[0].groupId").value("A"))
      .andExpect(jsonPath("$.messages[0].enqueuedAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.messages[0].availableAt").value("1970-01-01T00:00:30Z"))
      .andExpect(jsonPath("$.messages[0].attemptCount").value(10))
      .andExpect(jsonPath("$.messages[0].subject").value("TestEvent"));
  }

  @Test
  void requeueLockedMessageShouldReturnNoContent() throws Exception {
    // Arrange
    doNothing()
      .when(messageConsumer)
      .requeueLocked(eq(0L), eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));

    // Act
    // Assert
    mockMvc().perform(put("/outbox/messages/{sequence_number}/locks/{lock_id}", 0L, "00000000-0000-0000-0000-000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }

  @Test
  void dequeueLockedMessageShouldReturnNoContent() throws Exception {
    // Arrange
    doNothing()
      .when(messageConsumer)
      .dequeueLocked(eq(0L), eq(UUID.fromString("00000000-0000-0000-0000-000000000000")));

    // Act
    // Assert
    mockMvc().perform(delete("/outbox/messages/{sequence_number}/locks/{lock_id}", 0L, "00000000-0000-0000-0000-000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }
}
