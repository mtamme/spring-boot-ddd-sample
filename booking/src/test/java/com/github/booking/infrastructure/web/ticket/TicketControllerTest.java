package com.github.booking.infrastructure.web.ticket;

import com.github.booking.application.ticket.TicketCommandHandler;
import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.command.RedeemTicketCommand;
import com.github.booking.application.ticket.query.GetTicketQuery;
import com.github.booking.application.ticket.query.ListTicketsQuery;
import com.github.booking.application.ticket.query.SeatAssignmentView;
import com.github.booking.application.ticket.query.TicketBookingView;
import com.github.booking.application.ticket.query.TicketDetailView;
import com.github.booking.application.ticket.query.TicketSummaryView;
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
  private TicketCommandHandler bookingCommandHandler;
  @MockitoBean
  private TicketQueryHandler bookingQueryHandler;

  @Test
  void redeemTicketShouldReturnNoContent() throws Exception {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(RedeemTicketCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .redeemTicket(commandCaptor.capture());

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
    final var queryCaptor = ArgumentCaptor.forClass(GetTicketQuery.class);

    when(bookingQueryHandler.getTicket(queryCaptor.capture()))
      .thenReturn(new TicketDetailView(
        "T00000000000000000",
        "ISSUED",
        TicketBookingView.of("B00000000000000000", "COMPLETED"),
        SeatAssignmentView.of("TestTitle", "TestName", Instant.EPOCH, "A1")));

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
    final var queryCaptor = ArgumentCaptor.forClass(ListTicketsQuery.class);

    when(bookingQueryHandler.listTickets(queryCaptor.capture()))
      .thenReturn(List.of(new TicketSummaryView(
        "T00000000000000000",
        "ISSUED",
        TicketBookingView.of("B00000000000000000", "COMPLETED"),
        SeatAssignmentView.of("TestTitle", "TestName", Instant.EPOCH, "A1"))));

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
