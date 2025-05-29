package com.github.booking.domain.ticket;

import com.github.booking.domain.booking.BookingId;
import com.github.seedwork.domain.AggregateRoot;
import com.github.seedwork.domain.Contract;

import java.util.Objects;

public class Ticket extends AggregateRoot {

  private BookingId bookingId;
  private TicketId ticketId;
  private TicketStatus status;
  private ShowAssignment showAssignment;

  public Ticket(final BookingId bookingId,
                final TicketId ticketId,
                final ShowAssignment showAssignment) {
    Contract.require(bookingId != null);
    Contract.require(ticketId != null);
    Contract.require(showAssignment != null);

    this.bookingId = bookingId;
    this.ticketId = ticketId;
    this.status = TicketStatus.ISSUED;
    this.showAssignment = showAssignment;

    raiseEvent(new TicketIssued(ticketId()));
  }

  public BookingId bookingId() {
    return bookingId;
  }

  public TicketId ticketId() {
    return ticketId;
  }

  public boolean isIssued() {
    return status().isIssued();
  }

  public boolean isRedeemed() {
    return status().isRedeemed();
  }

  public TicketStatus status() {
    return status;
  }

  public ShowAssignment showAssignment() {
    return showAssignment;
  }

  public void redeem() {
    Contract.check(isIssued() || isRedeemed(), TicketNotRedeemableException::new);

    if (isRedeemed()) {
      return;
    }
    markAsRedeemed();
  }

  private void markAsRedeemed() {
    status = TicketStatus.REDEEMED;
    raiseEvent(new TicketRedeemed(ticketId()));
  }

  private Long id;

  protected Ticket() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof final Ticket other)) {
      return false;
    }

    return Objects.equals(other.ticketId(), ticketId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(ticketId());
  }
}
