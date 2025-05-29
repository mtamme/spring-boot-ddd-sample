package com.github.booking.infrastructure.web.booking;

import com.github.booking.application.booking.command.InitiateBooking;
import com.github.booking.application.booking.query.GetBooking;
import com.github.booking.application.booking.query.ListBookings;
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

  public InitiateBookingResponse toInitiateBookingResponse(final InitiateBooking.Booking booking) {
    return new InitiateBookingResponse()
      .bookingId(booking.bookingId());
  }

  public GetBookingResponse toGetBookingResponse(final GetBooking.Booking booking) {
    return new GetBookingResponse()
      .bookingId(booking.bookingId())
      .status(booking.status())
      .show(Optional.ofNullable(booking.show())
        .map(bs -> new BookingShow()
          .showId(bs.showId())
          .scheduledAt(bs.scheduledAt()))
        .orElse(null));
  }

  public ListBookingsResponse toListBookingsResponse(final List<ListBookings.Booking> bookings) {
    return new ListBookingsResponse()
      .bookings(bookings.stream()
        .map(b -> new BookingSummary()
          .bookingId(b.bookingId())
          .status(b.status())
          .show(Optional.ofNullable(b.show())
            .map(bs -> new BookingShow()
              .showId(bs.showId())
              .scheduledAt(bs.scheduledAt()))
            .orElse(null)))
        .toList());
  }
}
