package com.github.booking.application.ticket;

import com.github.booking.application.ticket.command.RedeemTicketCommand;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.ticket.Ticket;
import com.github.booking.domain.ticket.TicketException;
import com.github.booking.domain.ticket.TicketFixture;
import com.github.booking.domain.ticket.TicketId;
import com.github.booking.domain.ticket.TicketRepository;
import com.github.booking.domain.ticket.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCommandHandlerImplTest {

  @Mock
  private TicketRepository ticketRepository;

  @Test
  void redeemTicketShouldRedeemTicket() {
    // Arrange
    when(ticketRepository.findByTicketId(new TicketId("T00000000000000000")))
      .thenReturn(Optional.of(TicketFixture.newIssuedTicket(
        "S00000000000000000",
        "B00000000000000000",
        "A1",
        "T00000000000000000")));
    final var ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

    doNothing()
      .when(ticketRepository)
      .save(ticketCaptor.capture());
    final var ticketCommandHandler = new TicketCommandHandlerImpl(ticketRepository);
    final var command = new RedeemTicketCommand("T00000000000000000");

    // Act
    // Assert
    ticketCommandHandler.redeemTicket(command);
    final var ticket = ticketCaptor.getValue();

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
  void redeemTicketWithUnknownTicketIdShouldThrowTicketException() {
    // Arrange
    when(ticketRepository.findByTicketId(new TicketId("T00000000000000000")))
      .thenReturn(Optional.empty());
    final var ticketCommandHandler = new TicketCommandHandlerImpl(ticketRepository);
    final var command = new RedeemTicketCommand("T00000000000000000");

    // Act
    // Assert
    final var exception = assertThrows(TicketException.class, () -> ticketCommandHandler.redeemTicket(command));

    assertEquals(TicketException.NOT_FOUND_PROBLEM, exception.getProblem());
  }
}
