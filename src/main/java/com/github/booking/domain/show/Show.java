package com.github.booking.domain.show;

import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.Hall;
import com.github.booking.domain.hall.SeatLayout;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.Movie;
import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.domain.Contract;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    Contract.require(showId != null);
    Contract.require(scheduledAt != null);
    Contract.require(movie != null);
    Contract.require(hall != null);

    this.showId = showId;
    this.scheduledAt = scheduledAt;
    this.movie = movie;
    this.hall = hall;
    this.seats = new LinkedHashSet<>();

    applySeatLayout(hall.seatLayout());
  }

  private void applySeatLayout(final SeatLayout seatLayout) {
    for (final var seatNumber : seatLayout.seatNumbers()) {
      final var seat = new Seat(this, seatNumber);

      seats.add(seat);
    }
  }

  public ShowId showId() {
    return showId;
  }

  public Instant scheduledAt() {
    return scheduledAt;
  }

  public Movie movie() {
    return movie;
  }

  public Hall hall() {
    return hall;
  }

  private Seat seat(final SeatNumber seatNumber) {
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

  public void reserveSeat(final BookingId bookingId, final SeatNumber seatNumber) {
    Contract.require(bookingId != null);
    Contract.require(seatNumber != null);
    final var seat = seat(seatNumber);

    seat.reserve(bookingId);
  }

  public void releaseSeat(final BookingId bookingId, final SeatNumber seatNumber) {
    Contract.require(bookingId != null);
    Contract.require(seatNumber != null);
    final var seat = seat(seatNumber);

    seat.release(bookingId);
  }

  public void bookSeats(final BookingId bookingId) {
    Contract.require(bookingId != null);
    final var seats = seatsAssignedTo(bookingId);

    for (final var seat : seats) {
      seat.book(bookingId);
    }
  }

  public void releaseSeats(final BookingId bookingId) {
    Contract.require(bookingId != null);
    final var seats = seatsAssignedTo(bookingId);

    for (final var seat : seats) {
      seat.release(bookingId);
    }
  }

  public Booking initiateBooking(final BookingId bookingId) {
    return new Booking(showId(), bookingId);
  }

  private Long id;

  protected Show() {
  }

  @Override
  public String aggregateId() {
    return showId().value();
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
