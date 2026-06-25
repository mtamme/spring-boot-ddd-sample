package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.query.ListTickets;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.ticket.TicketFixture;
import com.github.booking.infrastructure.persistence.PersistenceTest;
import com.github.booking.infrastructure.persistence.booking.JpaBookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaListTicketsTest extends PersistenceTest {

  @Autowired
  private JpaTicketRepository ticketRepository;
  @Autowired
  private JpaBookingRepository bookingRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void handleShouldReturnTickets() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
      final var ticket = TicketFixture.newIssuedTicket("S00000000000000000", "B00000000000000000", "A1", "T00000000000000000");

      ticketRepository.save(ticket);
    });
    final var handler = new JpaListTickets(ticketRepository);
    final var query = new ListTickets.Query(0L, 1);

    // Act
    final var tickets = handler.handle(query);

    // Assert
    assertEquals(1, tickets.size());
    final var ticket = tickets.getFirst();

    assertEquals("T00000000000000000", ticket.ticketId());
    assertEquals("ISSUED", ticket.status());
    final var booking = ticket.booking();

    assertEquals("B00000000000000000", booking.bookingId());
    assertEquals("CONFIRMED", booking.status());
    final var seatAssignment = ticket.seatAssignment();

    assertEquals("TestTitle", seatAssignment.movieTitle());
    assertEquals("TestName", seatAssignment.hallName());
    assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
    assertEquals("A1", seatAssignment.seatNumber());
  }
}
