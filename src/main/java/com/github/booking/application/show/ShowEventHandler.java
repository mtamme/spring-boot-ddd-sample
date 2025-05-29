package com.github.booking.application.show;

import com.github.booking.domain.booking.BookingCancelled;
import com.github.booking.domain.booking.BookingConfirmed;
import com.github.booking.domain.show.ShowNotFoundException;
import com.github.booking.domain.show.ShowRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class ShowEventHandler {

  private final ShowRepository showRepository;

  public ShowEventHandler(final ShowRepository showRepository) {
    this.showRepository = Objects.requireNonNull(showRepository);
  }

  @EventListener
  public void onBookingConfirmed(final BookingConfirmed event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowNotFoundException::new);

    show.bookSeats(event.bookingId());
    showRepository.save(show);
  }

  @EventListener
  public void onBookingCancelled(final BookingCancelled event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowNotFoundException::new);

    show.releaseSeats(event.bookingId());
    showRepository.save(show);
  }
}
