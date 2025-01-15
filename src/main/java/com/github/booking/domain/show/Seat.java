package com.github.booking.domain.show;

import com.github.booking.domain.booking.BookingId;
import com.github.booking.domain.hall.SeatNumber;
import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.Entity;

import java.util.Objects;

public class Seat implements Entity {

  private Show show;
  private SeatNumber seatNumber;
  private SeatStatus status;
  private BookingId bookingId;

  public Seat(final Show show, final SeatNumber seatNumber) {
    setShow(show);
    setSeatNumber(seatNumber);

    markAsAvailable();
  }

  public Show show() {
    return show;
  }

  private void setShow(final Show show) {
    this.show = Contract.requireNonNull(show);
  }

  public SeatNumber seatNumber() {
    return seatNumber;
  }

  private void setSeatNumber(final SeatNumber seatNumber) {
    this.seatNumber = Contract.requireNonNull(seatNumber);
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

  private void setStatus(final SeatStatus status) {
    this.status = Contract.requireNonNull(status);
  }

  public BookingId bookingId() {
    return bookingId;
  }

  private void setBookingId(final BookingId bookingId) {
    this.bookingId = bookingId;
  }

  public boolean isAssignedTo(final BookingId bookingId) {
    return Objects.equals(bookingId, bookingId());
  }

  public void reserve(final BookingId bookingId) {
    Contract.require(isAvailable() || (isAssignedTo(bookingId) && isReserved()), SeatNotReservableException::new);

    if (isReserved()) {
      return;
    }
    markAsReserved(bookingId);
  }

  private void markAsReserved(final BookingId bookingId) {
    setBookingId(bookingId);
    setStatus(SeatStatus.RESERVED);
  }

  public void book(final BookingId bookingId) {
    Contract.require(isAssignedTo(bookingId) && (isReserved() || isBooked()), SeatNotBookableException::new);

    if (isBooked()) {
      return;
    }
    markAsBooked();
  }

  private void markAsBooked() {
    setStatus(SeatStatus.BOOKED);
  }

  public void release(final BookingId bookingId) {
    Contract.require((isAssignedTo(bookingId) && isReserved()) || isAvailable(), SeatNotReleasableException::new);

    if (isAvailable()) {
      return;
    }
    markAsAvailable();
  }

  private void markAsAvailable() {
    setBookingId(null);
    setStatus(SeatStatus.AVAILABLE);
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
