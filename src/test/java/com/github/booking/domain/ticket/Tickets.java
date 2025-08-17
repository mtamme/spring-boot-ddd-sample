package com.github.booking.domain.ticket;

import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.show.Shows;
import com.github.seedwork.core.util.Consumers;

public final class Tickets {

  private Tickets() {
  }

  public static Ticket newRedeemedTicket(final String showId,
                                         final String bookingId,
                                         final String seatNumber,
                                         final String ticketId) {
    final var ticket = newIssuedTicket(showId, bookingId, seatNumber, ticketId);

    ticket.redeem();
    ticket.releaseEvents(Consumers.empty());

    return ticket;
  }

  public static Ticket newIssuedTicket(final String showId,
                                       final String bookingId,
                                       final String seatNumber,
                                       final String ticketId) {
    final var show = Shows.newShowWithBookedSeats(showId, bookingId, seatNumber);
    final var ticket = show.issueTicket(new TicketId(ticketId), new SeatNumber(seatNumber));

    ticket.releaseEvents(Consumers.empty());

    return ticket;
  }
}
