package com.github.booking.application.booking;

import com.github.booking.application.booking.query.BookingDetailView;
import com.github.booking.application.booking.query.BookingSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface BookingQueryHandler {

  BookingDetailView getBooking(String bookingId);

  List<BookingSummaryView> listBookings(long offset, int limit);
}
