package com.github.booking.domain.booking;

import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowId;
import com.github.seedwork.core.util.Contract;
import com.github.seedwork.domain.AggregateRoot;

import java.util.Objects;

public class Booking extends AggregateRoot {

  private ShowId showId;
  private BookingId bookingId;
  private BookingStatus status;

  public Booking(final Show show, final BookingId bookingId) {
    setShow(show);
    setBookingId(bookingId);

    markAsInitiated();
  }

  private void markAsInitiated() {
    setStatus(BookingStatus.INITIATED);
    raiseEvent(new BookingInitiated(showId(), bookingId()));
  }

  private void setShow(final Show show) {
    setShowId(show.showId());
  }

  public ShowId showId() {
    return showId;
  }

  private void setShowId(final ShowId showId) {
    this.showId = Contract.requireNonNull(showId);
  }

  public BookingId bookingId() {
    return bookingId;
  }

  private void setBookingId(final BookingId bookingId) {
    this.bookingId = Contract.requireNonNull(bookingId);
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

  private void setStatus(final BookingStatus status) {
    this.status = Contract.requireNonNull(status);
  }

  public void confirm() {
    Contract.require(isInitiated() || isConfirmed(), BookingNotConfirmableException::new);

    if (isConfirmed()) {
      return;
    }
    markAsConfirmed();
  }

  private void markAsConfirmed() {
    setStatus(BookingStatus.CONFIRMED);
    raiseEvent(new BookingConfirmed(showId(), bookingId()));
  }

  public void cancel() {
    Contract.require(isInitiated() || isCancelled(), BookingNotCancelableException::new);

    if (isCancelled()) {
      return;
    }
    markAsCancelled();
  }

  private void markAsCancelled() {
    setStatus(BookingStatus.CANCELLED);
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
