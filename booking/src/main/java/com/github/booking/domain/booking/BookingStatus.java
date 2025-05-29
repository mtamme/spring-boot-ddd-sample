package com.github.booking.domain.booking;

import com.github.seedwork.domain.ValueObject;

public enum BookingStatus implements ValueObject {

  INITIATED {
    @Override
    public boolean isInitiated() {
      return true;
    }
  },
  CONFIRMED {
    @Override
    public boolean isConfirmed() {
      return true;
    }
  },
  CANCELLED {
    @Override
    public boolean isCancelled() {
      return true;
    }
  };

  public boolean isInitiated() {
    return false;
  }

  public boolean isConfirmed() {
    return false;
  }

  public boolean isCancelled() {
    return false;
  }
}
