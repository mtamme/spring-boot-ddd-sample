package com.github.booking.infrastructure.web;

import com.github.booking.application.booking.command.InitiateBookingResult;
import com.github.booking.application.booking.query.BookingDetailView;
import com.github.booking.application.booking.query.BookingSummaryView;
import com.github.booking.infrastructure.web.representation.BookingShow;
import com.github.booking.infrastructure.web.representation.BookingSummary;
import com.github.booking.infrastructure.web.representation.GetBookingResponse;
import com.github.booking.infrastructure.web.representation.InitiateBookingResponse;
import com.github.booking.infrastructure.web.representation.ListBookingsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BookingMapper {

  private List<BookingSummary> toBookingSummaries(final List<BookingSummaryView> bookings) {
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

  public InitiateBookingResponse toInitiateBookingResponse(final InitiateBookingResult booking) {
    return new InitiateBookingResponse()
      .bookingId(booking.bookingId());
  }

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

  public ListBookingsResponse toListBookingsResponse(final List<BookingSummaryView> bookings) {
    return new ListBookingsResponse()
      .bookings(toBookingSummaries(bookings));
  }
}
