package com.github.booking.infrastructure.web;

import com.github.booking.application.booking.BookingCommandHandler;
import com.github.booking.application.booking.BookingQueryHandler;
import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import com.github.booking.application.booking.query.GetBookingQuery;
import com.github.booking.application.booking.query.ListBookingsQuery;
import com.github.booking.infrastructure.web.representation.GetBookingResponse;
import com.github.booking.infrastructure.web.representation.InitiateBookingResponse;
import com.github.booking.infrastructure.web.representation.ListBookingsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class BookingController implements BookingOperations {

  private final BookingCommandHandler bookingCommandHandler;
  private final BookingQueryHandler bookingQueryHandler;
  private final BookingMapper bookingMapper;

  public BookingController(final BookingCommandHandler bookingCommandHandler,
                           final BookingQueryHandler bookingQueryHandler,
                           final BookingMapper bookingMapper) {
    this.bookingCommandHandler = Objects.requireNonNull(bookingCommandHandler);
    this.bookingQueryHandler = Objects.requireNonNull(bookingQueryHandler);
    this.bookingMapper = Objects.requireNonNull(bookingMapper);
  }

  @Override
  public ResponseEntity<InitiateBookingResponse> initiateBooking(final String showId) {
    final var booking = bookingCommandHandler.initiateBooking(new InitiateBookingCommand(showId));
    final var body = bookingMapper.toInitiateBookingResponse(booking);

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(body);
  }

  @Override
  public ResponseEntity<Void> reserveSeat(final String bookingId, final String seatNumber) {
    bookingCommandHandler.reserveSeat(new ReserveSeatCommand(bookingId, seatNumber));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> releaseSeat(final String bookingId, final String seatNumber) {
    bookingCommandHandler.releaseSeat(new ReleaseSeatCommand(bookingId, seatNumber));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> confirmBooking(final String bookingId) {
    bookingCommandHandler.confirmBooking(new ConfirmBookingCommand(bookingId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<Void> cancelBooking(final String bookingId) {
    bookingCommandHandler.cancelBooking(new CancelBookingCommand(bookingId));

    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<GetBookingResponse> getBooking(final String bookingId) {
    final var booking = bookingQueryHandler.getBooking(new GetBookingQuery(bookingId));
    final var body = bookingMapper.toGetBookingResponse(booking);

    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListBookingsResponse> listBookings(final Long offset, final Integer limit) {
    final var bookings = bookingQueryHandler.listBookings(new ListBookingsQuery(offset, limit));
    final var body = bookingMapper.toListBookingsResponse(bookings);

    return ResponseEntity.ok(body);
  }
}
