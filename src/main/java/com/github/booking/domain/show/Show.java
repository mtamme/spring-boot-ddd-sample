package com.github.booking.domain.show;

import com.github.booking.domain.booking.Booking;
import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.Hall;
import com.github.booking.domain.hall.SeatLayout;
import com.github.booking.domain.hall.SeatNumber;
import com.github.booking.domain.movie.Movie;
import com.github.booking.domain.ticket.SeatAssignment;
import com.github.booking.domain.ticket.Ticket;
import com.github.booking.domain.ticket.TicketId;
import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.domain.Contract;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Show extends AggregateRoot {

  private ShowId showId;
  private Instant scheduledAt;
  private Movie movie;
  private Hall hall;
  private List<Seat> seats;

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
    this.seats = new ArrayList<>();

    applySeatLayout(hall.seatLayout());
  }

  private void applySeatLayout(final SeatLayout seatLayout) {
    for (final var seatNumber : seatLayout.seatNumbers()) {
      final var seat = new Seat(seatNumber);

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
      .orElseThrow(SeatException::notFound);
  }

  private List<Seat> seatsAssignedTo(final BookingId bookingId) {
    return seats.stream()
      .filter(s -> s.isAssignedTo(bookingId))
      .toList();
  }

  public List<Seat> seats() {
    return List.copyOf(seats);
  }

  public void reserveSeat(final Booking booking, final SeatNumber seatNumber) {
    Contract.require((booking != null) && booking.showId().equals(showId()));
    Contract.require(seatNumber != null);
    Contract.check(booking.isInitiated(), SeatException::notReservable);
    final var seat = seat(seatNumber);

    seat.reserve(this, booking.bookingId());
  }

  public void releaseSeat(final Booking booking, final SeatNumber seatNumber) {
    Contract.require((booking != null) && booking.showId().equals(showId()));
    Contract.require(seatNumber != null);
    Contract.check(booking.isInitiated() || booking.isCancelled(), SeatException::notReleasable);
    final var seat = seat(seatNumber);

    seat.release(this, booking.bookingId());
  }

  public void bookSeats(final Booking booking) {
    Contract.require((booking != null) && booking.showId().equals(showId()));
    Contract.check(booking.isInitiated() || booking.isConfirmed(), SeatException::notBookable);
    final var seats = seatsAssignedTo(booking.bookingId());

    for (final var seat : seats) {
      seat.book(this, booking.bookingId());
    }
  }

  public void releaseSeats(final Booking booking) {
    Contract.require((booking != null) && booking.showId().equals(showId()));
    Contract.check(booking.isInitiated() || booking.isCancelled(), SeatException::notReleasable);
    final var seats = seatsAssignedTo(booking.bookingId());

    for (final var seat : seats) {
      seat.release(this, booking.bookingId());
    }
  }

  public Booking initiateBooking(final BookingId bookingId) {
    return new Booking(showId(), bookingId);
  }

  public Ticket issueTicket(final TicketId ticketId, final SeatNumber seatNumber) {
    final var seat = seat(seatNumber);

    return new Ticket(
      seat.bookingId(),
      ticketId,
      new SeatAssignment(
        movie().title(),
        hall().name(),
        scheduledAt(),
        seat.seatNumber()));
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
