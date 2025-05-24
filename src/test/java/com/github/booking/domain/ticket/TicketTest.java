package com.github.booking.domain.ticket;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

  @Test
  void newTicketShouldReturnIssuedTicketAndRaiseTicketIssuedEvent() {
    // Arrange
    // Act
    final var ticket = new Ticket(
      new BookingId("B0000000000"),
      new TicketId("T0000000000"),
      new ShowAssignment(
        "TestTitle",
        "TestName",
        Instant.EPOCH,
        new SeatNumber("A1")));

    // Assert
    assertEquals(1, ticket.events().size());
    assertInstanceOf(TicketIssued.class, ticket.events().getFirst());
    assertEquals(new BookingId("B0000000000"), ticket.bookingId());
    assertEquals(new TicketId("T0000000000"), ticket.ticketId());
    assertEquals(TicketStatus.ISSUED, ticket.status());
    final var showAssignment = ticket.showAssignment();

    assertEquals("TestTitle", showAssignment.movieTitle());
    assertEquals("TestName", showAssignment.hallName());
    assertEquals(Instant.EPOCH, showAssignment.scheduledAt());
    assertEquals(new SeatNumber("A1"), showAssignment.seatNumber());
  }

  @Test
  void redeemWithIssuedTicketShouldRedeemTicketAndRaiseTicketRedeemedEvent() {
    // Arrange
    final var ticket = Tickets.newIssuedTicket(
      "S0000000000",
      "B0000000000",
      "A1",
      "T0000000000");

    // Act
    ticket.redeem();

    // Assert
    assertEquals(1, ticket.events().size());
    assertInstanceOf(TicketRedeemed.class, ticket.events().getFirst());
    assertEquals(new BookingId("B0000000000"), ticket.bookingId());
    assertEquals(new TicketId("T0000000000"), ticket.ticketId());
    assertEquals(TicketStatus.REDEEMED, ticket.status());
    final var showAssignment = ticket.showAssignment();

    assertEquals("TestTitle", showAssignment.movieTitle());
    assertEquals("TestName", showAssignment.hallName());
    assertEquals(Instant.EPOCH, showAssignment.scheduledAt());
    assertEquals(new SeatNumber("A1"), showAssignment.seatNumber());
  }

  @Test
  void redeemWithRedeemedTicketShouldDoNothingAndRaiseNoEvent() {
    // Arrange
    final var ticket = Tickets.newRedeemedTicket(
      "S0000000000",
      "B0000000000",
      "A1",
      "T0000000000");

    // Act
    ticket.redeem();

    // Assert
    assertEquals(0, ticket.events().size());
    assertEquals(new BookingId("B0000000000"), ticket.bookingId());
    assertEquals(new TicketId("T0000000000"), ticket.ticketId());
    assertEquals(TicketStatus.REDEEMED, ticket.status());
    final var showAssignment = ticket.showAssignment();

    assertEquals("TestTitle", showAssignment.movieTitle());
    assertEquals("TestName", showAssignment.hallName());
    assertEquals(Instant.EPOCH, showAssignment.scheduledAt());
    assertEquals(new SeatNumber("A1"), showAssignment.seatNumber());
  }
}
