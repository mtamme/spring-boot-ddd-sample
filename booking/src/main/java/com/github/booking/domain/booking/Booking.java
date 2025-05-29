package com.github.booking.domain.booking;

import com.github.booking.domain.show.ShowId;
import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.domain.Contract;

import java.util.Objects;

public class Booking extends AggregateRoot {

  private ShowId showId;
  private BookingId bookingId;
  private BookingStatus status;

  public Booking(final ShowId showId, final BookingId bookingId) {
    Contract.require(showId != null);
    Contract.require(bookingId != null);

    this.showId = showId;
    this.bookingId = bookingId;
    this.status = BookingStatus.INITIATED;

    raiseEvent(new BookingInitiated(showId(), bookingId()));
  }

  public ShowId showId() {
    return showId;
  }

  public BookingId bookingId() {
    return bookingId;
  }

  public boolean isInitiated() {
    return status().isInitiated();
  }

  public boolean isConfirmed() {
    return status().isConfirmed();
  }

  public boolean isCancelled() {
    return status().isCancelled();
  }

  public BookingStatus status() {
    return status;
  }

  public void confirm() {
    Contract.check(isInitiated() || isConfirmed(), BookingException::notConfirmable);

    if (isConfirmed()) {
      return;
    }
    markAsConfirmed();
  }

  private void markAsConfirmed() {
    status = BookingStatus.CONFIRMED;
    raiseEvent(new BookingConfirmed(showId(), bookingId()));
  }

  public void cancel() {
    Contract.check(isInitiated() || isCancelled(), BookingException::notCancelable);

    if (isCancelled()) {
      return;
    }
    markAsCancelled();
  }

  private void markAsCancelled() {
    status = BookingStatus.CANCELLED;
    raiseEvent(new BookingCancelled(showId(), bookingId()));
  }

  private Long id;

  protected Booking() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof final Booking other)) {
      return false;
    }

    return Objects.equals(other.bookingId(), bookingId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(bookingId());
  }
}
