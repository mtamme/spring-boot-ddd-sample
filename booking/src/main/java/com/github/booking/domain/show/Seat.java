package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.Entity;

import java.util.Objects;

public class Seat implements Entity {

  private SeatNumber seatNumber;
  private SeatStatus status;
  private BookingId bookingId;

  Seat(final SeatNumber seatNumber) {
    Contract.require(seatNumber != null);

    this.seatNumber = seatNumber;
    this.status = SeatStatus.AVAILABLE;
    this.bookingId = null;
  }

  public SeatNumber seatNumber() {
    return seatNumber;
  }

  public boolean isAvailable() {
    return status().isAvailable();
  }

  public boolean isReserved() {
    return status().isReserved();
  }

  public boolean isBooked() {
    return status().isBooked();
  }

  public SeatStatus status() {
    return status;
  }

  public BookingId bookingId() {
    return bookingId;
  }

  boolean isAssignedTo(final BookingId bookingId) {
    return Objects.equals(bookingId, bookingId());
  }

  void reserve(final Show show, final BookingId bookingId) {
    Contract.require(show != null);
    Contract.require(bookingId != null);
    Contract.check(isAvailable() || (isAssignedTo(bookingId) && isReserved()), SeatException::notReservable);

    if (isReserved()) {
      return;
    }
    markAsReserved(show, bookingId);
  }

  private void markAsReserved(final Show show, final BookingId bookingId) {
    this.bookingId = bookingId;
    this.status = SeatStatus.RESERVED;
    show.raiseEvent(new SeatReserved(show.showId(), seatNumber(), bookingId()));
  }

  void book(final Show show, final BookingId bookingId) {
    Contract.require(show != null);
    Contract.require(bookingId != null);
    Contract.check(isAssignedTo(bookingId) && (isReserved() || isBooked()), SeatException::notBookable);

    if (isBooked()) {
      return;
    }
    markAsBooked(show);
  }

  private void markAsBooked(final Show show) {
    status = SeatStatus.BOOKED;
    show.raiseEvent(new SeatBooked(show.showId(), seatNumber(), bookingId()));
  }

  void release(final Show show, final BookingId bookingId) {
    Contract.require(show != null);
    Contract.require(bookingId != null);
    Contract.check((isAssignedTo(bookingId) && isReserved()) || isAvailable(), SeatException::notReleasable);

    if (isAvailable()) {
      return;
    }
    markAsAvailable(show);
  }

  private void markAsAvailable(final Show show) {
    bookingId = null;
    status = SeatStatus.AVAILABLE;
    show.raiseEvent(new SeatReleased(show.showId(), seatNumber()));
  }

  protected Seat() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof final Seat other)) {
      return false;
    }

    return Objects.equals(other.seatNumber(), seatNumber());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(seatNumber());
  }
}
