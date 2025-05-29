package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.Entity;

import java.util.Objects;

public class Seat implements Entity {

  private Show show;
  private SeatNumber seatNumber;
  private SeatStatus status;
  private BookingId bookingId;

  Seat(final Show show, final SeatNumber seatNumber) {
    Contract.require(show != null);
    Contract.require(seatNumber != null);

    this.show = show;
    this.seatNumber = seatNumber;
    this.status = SeatStatus.AVAILABLE;
    this.bookingId = null;
  }

  public ShowId showId() {
    return show().showId();
  }

  private Show show() {
    return show;
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

  void reserve(final BookingId bookingId) {
    Contract.require(bookingId != null);
    Contract.check(isAvailable() || (isAssignedTo(bookingId) && isReserved()), SeatNotReservableException::new);

    if (isReserved()) {
      return;
    }
    markAsReserved(bookingId);
  }

  private void markAsReserved(final BookingId bookingId) {
    this.bookingId = bookingId;
    this.status = SeatStatus.RESERVED;
    show().recordEvent(new SeatReserved(showId(), seatNumber(), bookingId()));
  }

  void book(final BookingId bookingId) {
    Contract.require(bookingId != null);
    Contract.check(isAssignedTo(bookingId) && (isReserved() || isBooked()), SeatNotBookableException::new);

    if (isBooked()) {
      return;
    }
    markAsBooked();
  }

  private void markAsBooked() {
    status = SeatStatus.BOOKED;
    show().recordEvent(new SeatBooked(showId(), seatNumber(), bookingId()));
  }

  void release(final BookingId bookingId) {
    Contract.require(bookingId != null);
    Contract.check((isAssignedTo(bookingId) && isReserved()) || isAvailable(), SeatNotReleasableException::new);

    if (isAvailable()) {
      return;
    }
    markAsAvailable();
  }

  private void markAsAvailable() {
    bookingId = null;
    status = SeatStatus.AVAILABLE;
    show().recordEvent(new SeatReleased(showId(), seatNumber()));
  }

  private Long id;

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
