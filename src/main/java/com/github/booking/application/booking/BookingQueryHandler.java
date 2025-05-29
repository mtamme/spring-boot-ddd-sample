package com.github.booking.application.booking;

import com.github.booking.application.booking.query.BookingDetailView;
import com.github.booking.application.booking.query.BookingSummaryView;
import com.github.booking.application.booking.query.GetBookingQuery;
import com.github.booking.application.booking.query.ListBookingsQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface BookingQueryHandler {

  BookingDetailView getBooking(GetBookingQuery query);

  List<BookingSummaryView> listBookings(ListBookingsQuery query);
}
