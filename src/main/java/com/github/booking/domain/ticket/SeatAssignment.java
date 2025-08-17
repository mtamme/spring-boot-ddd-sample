package com.github.booking.domain.ticket;

import com.github.booking.domain.hall.SeatNumber;
import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

import java.time.Instant;

public record SeatAssignment(String movieTitle,
                             String hallName,
                             Instant scheduledAt,
                             SeatNumber seatNumber) implements ValueObject {

  public SeatAssignment {
    Contract.require(movieTitle != null);
    Contract.require(hallName != null);
    Contract.require(scheduledAt != null);
    Contract.require(seatNumber != null);
  }
}
