package com.github.booking.application.ticket;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.show.SeatBooked;
import com.github.booking.domain.show.ShowFixture;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import com.github.booking.domain.ticket.Ticket;
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
class TicketEventHandlerTest {

  @Mock
  private TicketRepository ticketRepository;
  @Mock
  private ShowRepository showRepository;

  @Test
  void onSeatBookedShouldIssueTicket() {
    // Arrange
    when(showRepository.findByShowId(new ShowId("S00000000000000000")))
      .thenReturn(Optional.of(ShowFixture.newShowWithBookedSeats("S00000000000000000", "B00000000000000000", "A1")));
    when(ticketRepository.nextTicketId())
      .thenReturn(new TicketId("T00000000000000000"));
    final var ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

    doNothing()
      .when(ticketRepository)
      .save(ticketCaptor.capture());
    final var ticketEventHandler = new TicketEventHandler(ticketRepository, showRepository);
    final var event = new SeatBooked(new ShowId("S00000000000000000"), new SeatNumber("A1"), new BookingId("B00000000000000000"));

    // Act
    ticketEventHandler.onSeatBooked(event);

    // Assert
    final var ticket = ticketCaptor.getValue();

    assertEquals(new BookingId("B00000000000000000"), ticket.bookingId());
    assertEquals(new TicketId("T00000000000000000"), ticket.ticketId());
    assertEquals(TicketStatus.ISSUED, ticket.status());
    final var seatAssignment = ticket.seatAssignment();

    assertEquals("TestTitle", seatAssignment.movieTitle());
    assertEquals("TestName", seatAssignment.hallName());
    assertEquals(Instant.EPOCH, seatAssignment.scheduledAt());
    assertEquals(new SeatNumber("A1"), seatAssignment.seatNumber());
  }
}
