package com.github.booking.application.show;

import com.github.booking.domain.booking.BookingCancelled;
import com.github.booking.domain.booking.BookingConfirmed;
import com.github.booking.domain.booking.BookingException;
import com.github.booking.domain.booking.BookingRepository;
import com.github.booking.domain.show.ShowException;
import com.github.booking.domain.show.ShowRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class ShowEventHandler {

  private final ShowRepository showRepository;
  private final BookingRepository bookingRepository;

  public ShowEventHandler(final ShowRepository showRepository, final BookingRepository bookingRepository) {
    this.showRepository = Objects.requireNonNull(showRepository);
    this.bookingRepository = Objects.requireNonNull(bookingRepository);
  }

  @EventListener
  public void onBookingConfirmed(final BookingConfirmed event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowException::notFound);
    final var booking = bookingRepository.findByBookingId(event.bookingId())
      .orElseThrow(BookingException::notFound);

    show.bookSeats(booking);
    showRepository.save(show);
  }

  @EventListener
  public void onBookingCancelled(final BookingCancelled event) {
    final var show = showRepository.findByShowId(event.showId())
      .orElseThrow(ShowException::notFound);
    final var booking = bookingRepository.findByBookingId(event.bookingId())
      .orElseThrow(BookingException::notFound);

    show.releaseSeats(booking);
    showRepository.save(show);
  }
}
