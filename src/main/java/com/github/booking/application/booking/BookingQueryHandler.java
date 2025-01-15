package com.github.booking.application.booking;

import com.github.booking.application.booking.view.BookingDetailView;
import com.github.booking.application.booking.view.BookingSummaryView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface BookingQueryHandler {

  BookingDetailView getBooking(String bookingId);

  List<BookingSummaryView> listBookings(long offset, int limit);
}
