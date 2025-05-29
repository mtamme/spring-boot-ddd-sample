package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;

import java.util.Optional;

public interface ShowRepository {

  Optional<Show> findByShowId(ShowId showId);

  Optional<Show> findByBookingId(BookingId bookingId);

  void save(Show show);
}
