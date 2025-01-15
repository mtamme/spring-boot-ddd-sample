package com.github.booking.api;

import com.github.booking.api.representation.BookingShow;
import com.github.booking.api.representation.BookingSummary;
import com.github.booking.api.representation.GetBookingResponse;
import com.github.booking.api.representation.InitiateBookingResponse;
import com.github.booking.api.representation.ListBookingsResponse;
import com.github.booking.application.booking.BookingCommandHandler;
import com.github.booking.application.booking.BookingQueryHandler;
import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
public class BookingController implements BookingOperations {

  private final BookingCommandHandler bookingCommandHandler;
  private final BookingQueryHandler bookingQueryHandler;

  public BookingController(final BookingCommandHandler bookingCommandHandler,
                           final BookingQueryHandler bookingQueryHandler) {
    this.bookingCommandHandler = Objects.requireNonNull(bookingCommandHandler);
    this.bookingQueryHandler = Objects.requireNonNull(bookingQueryHandler);
  }

  @Override
  public ResponseEntity<GetBookingResponse> getBooking(final String bookingId) {
    final var booking = bookingQueryHandler.getBooking(bookingId);
    final var response = new GetBookingResponse()
      .bookingId(booking.bookingId())
      .status(booking.status())
      .show(Optional.ofNullable(booking.show())
        .map(s -> new BookingShow()
          .showId(s.showId())
          .scheduledAt(s.scheduledAt()))
        .orElse(null));

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ListBookingsResponse> listBookings(final Long offset, final Integer limit) {
    final var bookings = bookingQueryHandler.listBookings(offset, limit)
      .stream()
      .map(b -> new BookingSummary()
        .bookingId(b.bookingId())
        .status(b.status())
        .show(Optional.ofNullable(b.show())
          .map(bs -> new BookingShow()
            .showId(bs.showId())
            .scheduledAt(bs.scheduledAt()))
          .orElse(null)))
      .toList();
    final var response = new ListBookingsResponse()
      .bookings(bookings);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<InitiateBookingResponse> initiateBooking(final String showId) {
    final var bookingId = bookingCommandHandler.initiateBooking(new InitiateBookingCommand(showId));
    final var response = new InitiateBookingResponse()
      .bookingId(bookingId);

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(response);
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
}
