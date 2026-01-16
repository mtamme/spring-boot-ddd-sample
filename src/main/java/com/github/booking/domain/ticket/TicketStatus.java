package com.github.booking.domain.ticket;

import com.github.seedwork.domain.ValueObject;

public enum TicketStatus implements ValueObject {

  ISSUED {
    @Override
    public boolean isIssued() {
      return true;
    }
  },
  REDEEMED {
    @Override
    public boolean isRedeemed() {
      return true;
    }
  };

  public boolean isIssued() {
    return false;
  }

  public boolean isRedeemed() {
    return false;
  }
}
