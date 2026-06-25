package com.github.booking.application.ticket.query;

import com.github.seedwork.application.QueryHandler;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Transactional(readOnly = true)
public interface GetTicket extends QueryHandler<GetTicket.Query, GetTicket.Ticket> {

  record Query(String ticketId) {
  }

  record Ticket(String ticketId,
                String status,
                Booking booking,
                SeatAssignment seatAssignment) {

    public Ticket(final String ticketId,
                  final String status,
                  final String bookingId,
                  final String bookingStatus,
                  final String seatAssignmentMovieTitle,
                  final String seatAssignmentHallName,
                  final Instant seatAssignmentScheduledAt,
                  final String seatAssignmentSeatNumber) {
      this(
        ticketId,
        status,
        new Booking(
          bookingId,
          bookingStatus),
        new SeatAssignment(
          seatAssignmentMovieTitle,
          seatAssignmentHallName,
          seatAssignmentScheduledAt,
          seatAssignmentSeatNumber));
    }
  }

  record Booking(String bookingId, String status) {
  }

  record SeatAssignment(String movieTitle,
                        String hallName,
                        Instant scheduledAt,
                        String seatNumber) {
  }
}
