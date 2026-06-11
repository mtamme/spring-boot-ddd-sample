package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.application.ticket.query.GetTicket;
import com.github.booking.domain.booking.BookingFixture;
import com.github.booking.domain.ticket.TicketFixture;
import com.github.booking.infrastructure.persistence.PersistenceTest;
import com.github.booking.infrastructure.persistence.booking.JpaBookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaGetTicketTest extends PersistenceTest {

  @Autowired
  private JpaTicketRepository ticketRepository;
  @Autowired
  private JpaBookingRepository bookingRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void handleShouldReturnTicket() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var booking = BookingFixture.newConfirmedBooking("S00000000000000000", "B00000000000000000");

      bookingRepository.save(booking);
      final var ticket = TicketFixture.newIssuedTicket("S00000000000000000", "B00000000000000000", "A1", "T00000000000000000");

      ticketRepository.save(ticket);
    });
    final var handler = new JpaGetTicket(ticketRepository);
    final var query = new GetTicket.Query("T00000000000000000");

    // Act
    final var ticket = handler.handle(query);

    // Assert
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
