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
      new BookingId("B00000000000000000"),
      new TicketId("T00000000000000000"),
      new SeatAssignment(
        "TestTitle",
        "TestName",
        Instant.EPOCH,
        new SeatNumber("A1")));

    // Assert
    assertEquals(1, ticket.raisedEvents().size());
    assertInstanceOf(TicketIssued.class, ticket.raisedEvents().getFirst());
    assertEquals(new BookingId("B00000000000000000"), ticket.bookingId());
    assertEquals(new TicketId("T00000000000000000"), ticket.ticketId());
    assertEquals(TicketStatus.ISSUED, ticket.status());
    final var seatAssignment = ticket.seatAssignment();

    assertEquals("TestTitle", seatAssignment.movieTitle());
    assertEquals("TestName", seatAssignment.hallName());
    assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
    assertEquals(new SeatNumber("A1"), seatAssignment.seatNumber());
  }

  @Test
  void redeemWithIssuedTicketShouldRedeemTicketAndRaiseTicketRedeemedEvent() {
    // Arrange
    final var ticket = TicketFixture.newIssuedTicket(
      "S00000000000000000",
      "B00000000000000000",
      "A1",
      "T00000000000000000");

    // Act
    ticket.redeem();

    // Assert
    assertEquals(1, ticket.raisedEvents().size());
    assertInstanceOf(TicketRedeemed.class, ticket.raisedEvents().getFirst());
    assertEquals(new BookingId("B00000000000000000"), ticket.bookingId());
    assertEquals(new TicketId("T00000000000000000"), ticket.ticketId());
    assertEquals(TicketStatus.REDEEMED, ticket.status());
    final var seatAssignment = ticket.seatAssignment();

    assertEquals("TestTitle", seatAssignment.movieTitle());
    assertEquals("TestName", seatAssignment.hallName());
    assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
    assertEquals(new SeatNumber("A1"), seatAssignment.seatNumber());
  }

  @Test
  void redeemWithRedeemedTicketShouldDoNothingAndNotRaiseEvent() {
    // Arrange
    final var ticket = TicketFixture.newRedeemedTicket(
      "S00000000000000000",
      "B00000000000000000",
      "A1",
      "T00000000000000000");

    // Act
    ticket.redeem();

    // Assert
    assertEquals(0, ticket.raisedEvents().size());
    assertEquals(new BookingId("B00000000000000000"), ticket.bookingId());
    assertEquals(new TicketId("T00000000000000000"), ticket.ticketId());
    assertEquals(TicketStatus.REDEEMED, ticket.status());
    final var seatAssignment = ticket.seatAssignment();

    assertEquals("TestTitle", seatAssignment.movieTitle());
    assertEquals("TestName", seatAssignment.hallName());
    assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
    assertEquals(new SeatNumber("A1"), seatAssignment.seatNumber());
  }
}
