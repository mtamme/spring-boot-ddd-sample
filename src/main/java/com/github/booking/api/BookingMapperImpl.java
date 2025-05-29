package com.github.booking.api;

import com.github.booking.api.representation.BookingShow;
import com.github.booking.api.representation.BookingSummary;
import com.github.booking.api.representation.GetBookingResponse;
import com.github.booking.application.booking.view.BookingDetailView;
import com.github.booking.application.booking.view.BookingSummaryView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BookingMapperImpl implements BookingMapper {

  @Override
  public GetBookingResponse toGetBookingResponse(final BookingDetailView booking) {
    return new GetBookingResponse()
      .bookingId(booking.bookingId())
      .status(booking.status())
      .show(Optional.ofNullable(booking.show())
        .map(bs -> new BookingShow()
          .showId(bs.showId())
          .scheduledAt(bs.scheduledAt()))
        .orElse(null));
  }

  @Override
  public List<BookingSummary> toBookingSummaries(final List<BookingSummaryView> bookings) {
    return bookings.stream()
      .map(b -> new BookingSummary()
        .bookingId(b.bookingId())
        .status(b.status())
        .show(Optional.ofNullable(b.show())
          .map(bs -> new BookingShow()
            .showId(bs.showId())
            .scheduledAt(bs.scheduledAt()))
          .orElse(null)))
      .toList();
  }
}
