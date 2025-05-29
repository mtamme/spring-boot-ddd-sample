package com.github.booking.api;

import com.github.booking.api.representation.BookingSummary;
import com.github.booking.api.representation.GetBookingResponse;
import com.github.booking.api.representation.ListBookingsResponse;
import com.github.booking.application.booking.view.BookingDetailView;
import com.github.booking.application.booking.view.BookingSummaryView;

import java.util.List;

public interface BookingMapper {

  GetBookingResponse toGetBookingResponse(BookingDetailView booking);

  default ListBookingsResponse toListBookingsResponse(final List<BookingSummaryView> bookings) {
    return new ListBookingsResponse()
      .bookings(toBookingSummaries(bookings));
  }

  List<BookingSummary> toBookingSummaries(List<BookingSummaryView> bookings);
}
