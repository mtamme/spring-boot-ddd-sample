package com.github.booking.application.show.policy;

import com.github.booking.domain.booking.BookingCancelled;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowRepository;
import com.github.seedwork.application.EventHandler;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
class ReleaseSeatsWhenBookingCancelled implements EventHandler<BookingCancelled> {

  private final ShowRepository showRepository;

  public ReleaseSeatsWhenBookingCancelled(final ShowRepository showRepository) {
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @EventListener
  @Override
  public void handle(final BookingCancelled event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowException::notFound);

    show.releaseSeats(event.bookingId());
    showRepository.save(show);
  }
}
