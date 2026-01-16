package com.github.booking.infrastructure.persistence.ticket;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.ticket.TicketFixture;
import com.github.booking.domain.ticket.TicketId;
import com.github.booking.domain.ticket.TicketStatus;
import com.github.seedwork.infrastructure.persistence.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JpaTicketRepositoryTest extends PersistenceTest {

  @Autowired
  private JpaTicketRepository ticketRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void findByTicketIdShouldReturnTicket() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var ticket = TicketFixture.newIssuedTicket(
        "S00000000000000000",
        "B00000000000000000",
        "A1",
        "T00000000000000000");

      ticketRepository.save(ticket);
    });

    // Act
    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var ticket = ticketRepository.findByTicketId(new TicketId("T00000000000000000"));

      assertTrue(ticket.isPresent());
      assertEquals(new BookingId("B00000000000000000"), ticket.get().bookingId());
      assertEquals(new TicketId("T00000000000000000"), ticket.get().ticketId());
      assertEquals(TicketStatus.ISSUED, ticket.get().status());
      final var seatAssignment = ticket.get().seatAssignment();

      assertEquals("TestTitle", seatAssignment.movieTitle());
      assertEquals("TestName", seatAssignment.hallName());
      assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
      assertEquals(new SeatNumber("A1"), seatAssignment.seatNumber());
    });
  }

  @Test
  void saveShouldSaveTicket() {
    // Arrange
    // Act
    transactionTemplate.executeWithoutResult(ts -> {
      final var ticket = TicketFixture.newIssuedTicket(
        "S00000000000000000",
        "B00000000000000000",
        "A1",
        "T00000000000000000");

      ticketRepository.save(ticket);
    });

    // Assert
    transactionTemplate.executeWithoutResult(ts -> {
      final var ticket = ticketRepository.findByTicketId(new TicketId("T00000000000000000"));

      assertTrue(ticket.isPresent());
      assertEquals(new BookingId("B00000000000000000"), ticket.get().bookingId());
      assertEquals(new TicketId("T00000000000000000"), ticket.get().ticketId());
      assertEquals(TicketStatus.ISSUED, ticket.get().status());
      final var seatAssignment = ticket.get().seatAssignment();

      assertEquals("TestTitle", seatAssignment.movieTitle());
      assertEquals("TestName", seatAssignment.hallName());
      assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
      assertEquals(new SeatNumber("A1"), seatAssignment.seatNumber());
    });
  }

  @Test
  void saveWithDuplicateTicketIdShouldThrowDataIntegrityViolationException() {
    // Arrange
    transactionTemplate.executeWithoutResult(ts -> {
      final var ticket = TicketFixture.newIssuedTicket(
        "S00000000000000000",
        "B00000000000000000",
        "A1",
        "T00000000000000000");

      ticketRepository.save(ticket);
    });

    // Act
    // Assert
    assertThrows(DataIntegrityViolationException.class, () -> transactionTemplate.executeWithoutResult(ts -> {
      final var ticket = TicketFixture.newIssuedTicket(
        "S00000000000000000",
        "B00000000000000000",
        "A1",
        "T00000000000000000");

      ticketRepository.save(ticket);
    }));
  }
}
