package com.github.booking.infrastructure.web.booking;

import com.github.booking.application.booking.command.CancelBooking;
import com.github.booking.application.booking.command.ConfirmBooking;
import com.github.booking.application.booking.command.InitiateBooking;
import com.github.booking.application.booking.command.ReleaseSeat;
import com.github.booking.application.booking.command.ReserveSeat;
import com.github.booking.application.booking.query.GetBooking;
import com.github.booking.application.booking.query.ListBookings;
import com.github.booking.infrastructure.web.BookingOperations;
import com.github.booking.infrastructure.web.representation.GetBookingResponse;
import com.github.booking.infrastructure.web.representation.InitiateBookingResponse;
import com.github.booking.infrastructure.web.representation.ListBookingsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class BookingController implements BookingOperations {

  private final InitiateBooking initiateBooking;
  private final ReserveSeat reserveSeat;
  private final ReleaseSeat releaseSeat;
  private final ConfirmBooking confirmBooking;
  private final CancelBooking cancelBooking;
  private final GetBooking getBooking;
  private final ListBookings listBookings;
  private final BookingMapper bookingMapper;

  public BookingController(final InitiateBooking initiateBooking,
                           final ReserveSeat reserveSeat,
                           final ReleaseSeat releaseSeat,
                           final ConfirmBooking confirmBooking,
                           final CancelBooking cancelBooking,
                           final GetBooking getBooking,
                           final ListBookings listBookings,
                           final BookingMapper bookingMapper) {
    this.initiateBooking = Objects.requireNonNull(initiateBooking);
    this.reserveSeat = Objects.requireNonNull(reserveSeat);
    this.releaseSeat = Objects.requireNonNull(releaseSeat);
    this.confirmBooking = Objects.requireNonNull(confirmBooking);
    this.cancelBooking = Objects.requireNonNull(cancelBooking);
    this.getBooking = Objects.requireNonNull(getBooking);
    this.listBookings = Objects.requireNonNull(listBookings);
    this.bookingMapper = Objects.requireNonNull(bookingMapper);
  }

  @Override
  public ResponseEntity<InitiateBookingResponse> initiateBooking(final String showId) {
    final var booking = initiateBooking.handle(new InitiateBooking.Command(showId));
    final var body = bookingMapper.toInitiateBookingResponse(booking);

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(body);
  }

  @Override
  public ResponseEntity<Void> reserveSeat(final String bookingId, final String seatNumber) {
    reserveSeat.handle(new ReserveSeat.Command(bookingId, seatNumber));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> releaseSeat(final String bookingId, final String seatNumber) {
    releaseSeat.handle(new ReleaseSeat.Command(bookingId, seatNumber));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> confirmBooking(final String bookingId) {
    confirmBooking.handle(new ConfirmBooking.Command(bookingId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> cancelBooking(final String bookingId) {
    cancelBooking.handle(new CancelBooking.Command(bookingId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<GetBookingResponse> getBooking(final String bookingId) {
    final var booking = getBooking.handle(new GetBooking.Query(bookingId));
    final var body = bookingMapper.toGetBookingResponse(booking);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListBookingsResponse> listBookings(final Long offset, final Integer limit) {
    final var bookings = listBookings.handle(new ListBookings.Query(offset, limit));
    final var body = bookingMapper.toListBookingsResponse(bookings);

    return ResponseEntity.ok(body);
  }
}
