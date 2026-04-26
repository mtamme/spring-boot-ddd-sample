package com.github.booking.application.ticket;

import com.github.booking.domain.show.SeatBooked;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowRepository;
import com.github.booking.domain.ticket.TicketRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class TicketEventHandler {

  private final TicketRepository ticketRepository;
  private final ShowRepository showRepository;

  public TicketEventHandler(final TicketRepository ticketRepository, final ShowRepository showRepository) {
    this.ticketRepository = Objects.requireNonNull(ticketRepository);
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @EventListener
  public void onSeatBooked(final SeatBooked event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowException::notFound);
    final var ticket = show.issueTicket(ticketRepository.nextTicketId(), event.seatNumber());

    ticketRepository.save(ticket);
  }
}
