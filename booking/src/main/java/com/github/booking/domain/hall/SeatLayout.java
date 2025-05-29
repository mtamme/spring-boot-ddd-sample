package com.github.booking.domain.hall;

import com.github.seedwork.domain.Contract;
import com.github.seedwork.domain.ValueObject;

import java.util.List;
import java.util.stream.IntStream;

public record SeatLayout(int rowCount, int columnCount) implements ValueObject {

  public SeatLayout {
    Contract.require((rowCount >= 1) && (rowCount <= 26));
    Contract.require((columnCount >= 1) && (columnCount <= 99));
  }

  public int seatCount() {
    return rowCount() * columnCount();
  }

  public List<SeatNumber> seatNumbers() {
    return IntStream.range(0, seatCount())
      .mapToObj(this::seatNumber)
      .toList();
  }

  private SeatNumber seatNumber(final int seatIndex) {
    final var rowIndex = seatIndex / columnCount();
    final var columnIndex = seatIndex % columnCount();

    return new SeatNumber("%c%d".formatted('A' + rowIndex, 1 + columnIndex));
  }
}
