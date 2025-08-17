package com.github.booking.api;

import com.github.booking.api.representation.GetTicketResponse;
import com.github.booking.api.representation.ListTicketsResponse;
import com.github.booking.application.ticket.TicketCommandHandler;
import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.command.RedeemTicketCommand;
import com.github.booking.application.ticket.view.SeatAssignmentView;
import com.github.booking.application.ticket.view.TicketBookingView;
import com.github.booking.application.ticket.view.TicketDetailView;
import com.github.booking.application.ticket.view.TicketSummaryView;
import com.github.seedwork.api.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketControllerTest extends ControllerTest {

  @MockitoBean
  private TicketCommandHandler bookingCommandHandler;
  @MockitoBean
  private TicketQueryHandler bookingQueryHandler;

  @Test
  void redeemTicketShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(RedeemTicketCommand.class);

    Mockito.doNothing()
      .when(bookingCommandHandler)
      .redeemTicket(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/redeemed-tickets/{ticket_id}", "T0000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("T0000000000", command.ticketId());
  }

  @Test
  void getTicketShouldReturnGetTicketResponse() {
    // Arrange
    Mockito.when(bookingQueryHandler.getTicket("T0000000000"))
      .thenReturn(new TicketDetailView(
        "T0000000000",
        "ISSUED",
        TicketBookingView.of("B0000000000", "COMPLETED"),
        SeatAssignmentView.of("TestTitle", "TestName", Instant.EPOCH, "A1")));

    // Act
    final var entity = client().get()
      .uri("/tickets/{ticket_id}", "T0000000000")
      .retrieve()
      .toEntity(GetTicketResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("T0000000000", response.getTicketId());
    assertEquals("ISSUED", response.getStatus());
    final var booking = response.getBooking();

    assertEquals("B0000000000", booking.getBookingId());
    assertEquals("COMPLETED", booking.getStatus());
    final var seatAssignment = response.getSeatAssignment();

    assertEquals("TestTitle", seatAssignment.getMovieTitle());
    assertEquals("TestName", seatAssignment.getHallName());
    assertEquals(Instant.EPOCH, seatAssignment.getScheduledAt());
    assertEquals("A1", seatAssignment.getSeatNumber());
  }

  @Test
  void listTicketsShouldReturnListTicketsResponse() {
    // Arrange
    Mockito.when(bookingQueryHandler.listTickets(0, 10))
      .thenReturn(List.of(new TicketSummaryView(
        "T0000000000",
        "ISSUED",
        TicketBookingView.of("B0000000000", "COMPLETED"),
        SeatAssignmentView.of("TestTitle", "TestName", Instant.EPOCH, "A1"))));

    // Act
    final var entity = client().get()
      .uri("/tickets")
      .retrieve()
      .toEntity(ListTicketsResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var tickets = response.getTickets();

    assertEquals(1, tickets.size());
    final var ticket = tickets.getFirst();

    assertEquals("T0000000000", ticket.getTicketId());
    assertEquals("ISSUED", ticket.getStatus());
    final var booking = ticket.getBooking();

    assertEquals("B0000000000", booking.getBookingId());
    assertEquals("COMPLETED", booking.getStatus());
    final var seatAssignment = ticket.getSeatAssignment();

    assertEquals("TestTitle", seatAssignment.getMovieTitle());
    assertEquals("TestName", seatAssignment.getHallName());
    assertEquals(Instant.EPOCH, seatAssignment.getScheduledAt());
    assertEquals("A1", seatAssignment.getSeatNumber());
  }
}
