package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.domain.booking.Bookings;
import com.github.booking.domain.ticket.Tickets;
import com.github.booking.infrastructure.persistence.booking.JpaBookingRepository;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaTicketQueryHandlerTest extends PersistenceTest {

  @Autowired
  private JpaTicketRepository ticketRepository;
  @Autowired
  private JpaBookingRepository bookingRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void getTicketShouldReturnTicket() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

      bookingRepository.save(booking);
      final var ticket = Tickets.newIssuedTicket("S0000000000", "B0000000000", "A1", "T0000000000");

      ticketRepository.save(ticket);
    });
    final var ticketQueryHandler = new JpaTicketQueryHandler(ticketRepository);

    // Act
    final var ticket = ticketQueryHandler.getTicket("T0000000000");

    // Assert
    assertEquals("T0000000000", ticket.ticketId());
    assertEquals("ISSUED", ticket.status());
    final var booking = ticket.booking();

    assertEquals("B0000000000", booking.bookingId());
    assertEquals("CONFIRMED", booking.status());
    final var seatAssignment = ticket.seatAssignment();

    assertEquals("TestTitle", seatAssignment.movieTitle());
    assertEquals("TestName", seatAssignment.hallName());
    assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
    assertEquals("A1", seatAssignment.seatNumber());
  }

  @Test
  void listTicketsShouldReturnTickets() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = Bookings.newConfirmedBooking("S0000000000", "B0000000000");

      bookingRepository.save(booking);
      final var ticket = Tickets.newIssuedTicket("S0000000000", "B0000000000", "A1", "T0000000000");

      ticketRepository.save(ticket);
    });
    final var ticketQueryHandler = new JpaTicketQueryHandler(ticketRepository);

    // Act
    final var tickets = ticketQueryHandler.listTickets(0L, 1);

    // Assert
    assertEquals(1, tickets.size());
    final var ticket = tickets.getFirst();

    assertEquals("T0000000000", ticket.ticketId());
    assertEquals("ISSUED", ticket.status());
    final var booking = ticket.booking();

    assertEquals("B0000000000", booking.bookingId());
    assertEquals("CONFIRMED", booking.status());
    final var seatAssignment = ticket.seatAssignment();

    assertEquals("TestTitle", seatAssignment.movieTitle());
    assertEquals("TestName", seatAssignment.hallName());
    assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
    assertEquals("A1", seatAssignment.seatNumber());
  }
}
