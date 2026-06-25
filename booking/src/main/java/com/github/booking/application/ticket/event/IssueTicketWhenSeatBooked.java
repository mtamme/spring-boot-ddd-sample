package com.github.booking.application.ticket.event;

import com.github.booking.domain.show.SeatBooked;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowRepository;
import com.github.booking.domain.ticket.TicketRepository;
import com.github.seedwork.application.EventHandler;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
class IssueTicketWhenSeatBooked implements EventHandler<SeatBooked> {

  private final TicketRepository ticketRepository;
  private final ShowRepository showRepository;

  public IssueTicketWhenSeatBooked(final TicketRepository ticketRepository, final ShowRepository showRepository) {
    this.ticketRepository = Objects.requireNonNull(ticketRepository);
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @EventListener
  @Override
  public void handle(final SeatBooked event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowException::notFound);
    final var ticket = show.issueTicket(ticketRepository.nextTicketId(), event.seatNumber());

    ticketRepository.save(ticket);
  }
}
