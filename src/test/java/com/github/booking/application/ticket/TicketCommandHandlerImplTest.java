package com.github.booking.application.ticket;

import com.github.booking.application.ticket.command.RedeemTicketCommand;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.ticket.Ticket;
import com.github.booking.domain.ticket.TicketId;
import com.github.booking.domain.ticket.TicketRepository;
import com.github.booking.domain.ticket.TicketStatus;
import com.github.booking.domain.ticket.Tickets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TicketCommandHandlerImplTest {

  @Mock
  private TicketRepository ticketRepository;

  @Test
  void redeemTicketShouldRedeemTicket() {
    // Arrange
    Mockito.when(ticketRepository.findByTicketId(new TicketId("T0000000000")))
      .thenReturn(Optional.of(Tickets.newIssuedTicket(
        "S0000000000",
        "B0000000000",
        "A1",
        "T0000000000")));
    final var ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

    Mockito.doNothing()
      .when(ticketRepository)
      .save(ticketCaptor.capture());
    final var ticketCommandHandler = new TicketCommandHandlerImpl(ticketRepository);
    final var command = new RedeemTicketCommand("T0000000000");

    // Act
    // Assert
    ticketCommandHandler.redeemTicket(command);
    final var ticket = ticketCaptor.getValue();

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
