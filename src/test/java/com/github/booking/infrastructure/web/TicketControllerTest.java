package com.github.booking.infrastructure.web;

import com.github.booking.application.ticket.TicketCommandHandler;
import com.github.booking.application.ticket.TicketQueryHandler;
import com.github.booking.application.ticket.command.RedeemTicketCommand;
import com.github.booking.application.ticket.query.GetTicketQuery;
import com.github.booking.application.ticket.query.ListTicketsQuery;
import com.github.booking.application.ticket.query.SeatAssignmentView;
import com.github.booking.application.ticket.query.TicketBookingView;
import com.github.booking.application.ticket.query.TicketDetailView;
import com.github.booking.application.ticket.query.TicketSummaryView;
import com.github.booking.infrastructure.web.representation.GetTicketResponse;
import com.github.booking.infrastructure.web.representation.ListTicketsResponse;
import com.github.seedwork.infrastructure.web.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketControllerTest extends ControllerTest {

  @MockitoBean
  private TicketCommandHandler bookingCommandHandler;
  @MockitoBean
  private TicketQueryHandler bookingQueryHandler;

  @Test
  void redeemTicketShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(RedeemTicketCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .redeemTicket(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/redeemed-tickets/{ticket_id}", "T00000000000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("T00000000000000000", command.ticketId());
  }

  @Test
  void getTicketShouldReturnGetTicketResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(GetTicketQuery.class);

    when(bookingQueryHandler.getTicket(queryCaptor.capture()))
      .thenReturn(new TicketDetailView(
        "T00000000000000000",
        "ISSUED",
        TicketBookingView.of("B00000000000000000", "COMPLETED"),
        SeatAssignmentView.of("TestTitle", "TestName", Instant.EPOCH, "A1")));

    // Act
    final var entity = client().get()
      .uri("/tickets/{ticket_id}", "T00000000000000000")
      .retrieve()
      .toEntity(GetTicketResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("T00000000000000000", response.getTicketId());
    assertEquals("ISSUED", response.getStatus());
    final var booking = response.getBooking();

    assertEquals("B00000000000000000", booking.getBookingId());
    assertEquals("COMPLETED", booking.getStatus());
    final var seatAssignment = response.getSeatAssignment();

    assertEquals("TestTitle", seatAssignment.getMovieTitle());
    assertEquals("TestName", seatAssignment.getHallName());
    assertEquals(Instant.EPOCH, seatAssignment.getScheduledAt());
    assertEquals("A1", seatAssignment.getSeatNumber());
    final var query = queryCaptor.getValue();

    assertEquals("T00000000000000000", query.ticketId());
  }

  @Test
  void listTicketsShouldReturnListTicketsResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListTicketsQuery.class);

    when(bookingQueryHandler.listTickets(queryCaptor.capture()))
      .thenReturn(List.of(new TicketSummaryView(
        "T00000000000000000",
        "ISSUED",
        TicketBookingView.of("B00000000000000000", "COMPLETED"),
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

    assertEquals("T00000000000000000", ticket.getTicketId());
    assertEquals("ISSUED", ticket.getStatus());
    final var booking = ticket.getBooking();

    assertEquals("B00000000000000000", booking.getBookingId());
    assertEquals("COMPLETED", booking.getStatus());
    final var seatAssignment = ticket.getSeatAssignment();

    assertEquals("TestTitle", seatAssignment.getMovieTitle());
    assertEquals("TestName", seatAssignment.getHallName());
    assertEquals(Instant.EPOCH, seatAssignment.getScheduledAt());
    assertEquals("A1", seatAssignment.getSeatNumber());
    final var query = queryCaptor.getValue();

    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }
}
