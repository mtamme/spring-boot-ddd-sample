package com.github.booking.application.show.event;

import com.github.booking.domain.booking.BookingConfirmed;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowRepository;
import com.github.seedwork.application.EventHandler;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
class BookSeatsWhenBookingConfirmed implements EventHandler<BookingConfirmed> {

  private final ShowRepository showRepository;

  public BookSeatsWhenBookingConfirmed(final ShowRepository showRepository) {
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @EventListener
  @Override
  public void handle(final BookingConfirmed event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowException::notFound);

    show.bookSeats(event.bookingId());
    showRepository.save(show);
  }
}
