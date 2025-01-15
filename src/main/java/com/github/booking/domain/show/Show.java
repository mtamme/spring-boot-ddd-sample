package com.github.booking.domain.show;

import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.Hall;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.Movie;
import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.AggregateRoot;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Show extends AggregateRoot {

  private ShowId showId;
  private Instant scheduledAt;
  private Movie movie;
  private Hall hall;
  private Set<Seat> seats;

  public Show(final ShowId showId,
              final Instant scheduledAt,
              final Movie movie,
              final Hall hall) {
    setShowId(showId);
    setScheduledAt(scheduledAt);
    setMovie(movie);
    setHall(hall);
    setSeats(hall);
  }

  public ShowId showId() {
    return showId;
  }

  private void setShowId(final ShowId showId) {
    this.showId = Contract.requireNonNull(showId);
  }

  public Instant scheduledAt() {
    return scheduledAt;
  }

  private void setScheduledAt(final Instant scheduledAt) {
    this.scheduledAt = Contract.requireNonNull(scheduledAt);
  }

  public Movie movie() {
    return movie;
  }

  private void setMovie(final Movie movie) {
    this.movie = Contract.requireNonNull(movie);
  }

  public Hall hall() {
    return hall;
  }

  private void setHall(final Hall hall) {
    this.hall = Contract.requireNonNull(hall);
  }

  public Seat seat(final SeatNumber seatNumber) {
    return seats.stream()
      .filter(s -> s.seatNumber().equals(seatNumber))
      .findFirst()
      .orElseThrow(SeatNotFoundException::new);
  }

  private List<Seat> seatsAssignedTo(final BookingId bookingId) {
    return seats.stream()
      .filter(s -> s.isAssignedTo(bookingId))
      .toList();
  }

  public List<Seat> seats() {
    return List.copyOf(seats);
  }

  private void setSeats(final Hall hall) {
    final var seats = hall.seatLayout()
      .seatNumbers()
      .stream()
      .map(si -> new Seat(this, si))
      .collect(Collectors.toCollection(LinkedHashSet::new));

    setSeats(seats);
  }

  private void setSeats(final Set<Seat> seats) {
    this.seats = Contract.requireNonNull(seats);
  }

  public void reserveSeat(final Booking booking, final SeatNumber seatNumber) {
    final var seat = seat(seatNumber);

    seat.reserve(booking.bookingId());
  }

  public void releaseSeat(final Booking booking, final SeatNumber seatNumber) {
    final var seat = seat(seatNumber);

    seat.release(booking.bookingId());
  }

  public void bookSeats(final Booking booking) {
    final var seats = seatsAssignedTo(booking.bookingId());

    for (final var seat : seats) {
      seat.book(booking.bookingId());
    }
  }

  public void releaseSeats(final Booking booking) {
    final var seats = seatsAssignedTo(booking.bookingId());

    for (final var seat : seats) {
      seat.release(booking.bookingId());
    }
  }

  private Long id;

  protected Show() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof final Show other)) {
      return false;
    }

    return Objects.equals(other.showId(), showId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(showId());
  }
}
