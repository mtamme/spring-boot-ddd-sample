package com.github.booking.domain.show;

import com.github.seedwork.domain.ValueObject;

public enum SeatStatus implements ValueObject {

  AVAILABLE {
    @Override
    public boolean isAvailable() {
      return true;
    }
  },
  RESERVED {
    @Override
    public boolean isReserved() {
      return true;
    }
  },
  BOOKED {
    @Override
    public boolean isBooked() {
      return true;
    }
  };

  public boolean isAvailable() {
    return false;
  }

  public boolean isReserved() {
    return false;
  }

  public boolean isBooked() {
    return false;
  }
}
