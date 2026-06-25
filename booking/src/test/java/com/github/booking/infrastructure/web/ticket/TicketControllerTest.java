package com.github.booking.infrastructure.web.ticket;

import com.github.booking.application.ticket.command.RedeemTicket;
import com.github.booking.application.ticket.query.GetTicket;
import com.github.booking.application.ticket.query.ListTickets;
import com.github.booking.infrastructure.web.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketControllerTest extends ControllerTest {

  @MockitoBean
  private RedeemTicket redeemTicket;
  @MockitoBean
  private GetTicket getTicket;
  @MockitoBean
  private ListTickets listTickets;

  @Test
  void redeemTicketShouldReturnNoContent() throws Exception {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(RedeemTicket.Command.class);

    doNothing()
      .when(redeemTicket)
      .handle(commandCaptor.capture());

    // Act
    // Assert
    mockMvc().perform(put("/redeemed-tickets/{ticket_id}", "T00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
    final var command = commandCaptor.getValue();

    assertEquals("T00000000000000000", command.ticketId());
  }

  @Test
  void getTicketShouldReturnGetTicketResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(GetTicket.Query.class);

    when(getTicket.handle(queryCaptor.capture()))
      .thenReturn(new GetTicket.Ticket(
        "T00000000000000000",
        "ISSUED",
        new GetTicket.Booking("B00000000000000000", "COMPLETED"),
        new GetTicket.SeatAssignment("TestTitle", "TestName", Instant.EPOCH, "A1")));

    // Act
    // Assert
    mockMvc().perform(get("/tickets/{ticket_id}", "T00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.ticketId").value("T00000000000000000"))
      .andExpect(jsonPath("$.status").value("ISSUED"))
      .andExpect(jsonPath("$.booking.bookingId").value("B00000000000000000"))
      .andExpect(jsonPath("$.booking.status").value("COMPLETED"))
      .andExpect(jsonPath("$.seatAssignment.movieTitle").value("TestTitle"))
      .andExpect(jsonPath("$.seatAssignment.hallName").value("TestName"))
      .andExpect(jsonPath("$.seatAssignment.scheduledAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.seatAssignment.seatNumber").value("A1"));
    final var query = queryCaptor.getValue();

    assertEquals("T00000000000000000", query.ticketId());
  }

  @Test
  void listTicketsShouldReturnListTicketsResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListTickets.Query.class);

    when(listTickets.handle(queryCaptor.capture()))
      .thenReturn(List.of(new ListTickets.Ticket(
        "T00000000000000000",
        "ISSUED",
        new ListTickets.Booking("B00000000000000000", "COMPLETED"),
        new ListTickets.SeatAssignment("TestTitle", "TestName", Instant.EPOCH, "A1"))));

    // Act
    // Assert
    mockMvc().perform(get("/tickets")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.tickets.length()").value(1))
      .andExpect(jsonPath("$.tickets[0].ticketId").value("T00000000000000000"))
      .andExpect(jsonPath("$.tickets[0].status").value("ISSUED"))
      .andExpect(jsonPath("$.tickets[0].booking.bookingId").value("B00000000000000000"))
      .andExpect(jsonPath("$.tickets[0].booking.status").value("COMPLETED"))
      .andExpect(jsonPath("$.tickets[0].seatAssignment.movieTitle").value("TestTitle"))
      .andExpect(jsonPath("$.tickets[0].seatAssignment.hallName").value("TestName"))
      .andExpect(jsonPath("$.tickets[0].seatAssignment.scheduledAt").value("1970-01-01T00:00:00Z"))
      .andExpect(jsonPath("$.tickets[0].seatAssignment.seatNumber").value("A1"));
    final var query = queryCaptor.getValue();

    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }
}
