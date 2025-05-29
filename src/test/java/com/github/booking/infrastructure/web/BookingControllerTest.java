package com.github.booking.infrastructure.web;

import com.github.booking.application.booking.BookingCommandHandler;
import com.github.booking.application.booking.BookingQueryHandler;
import com.github.booking.application.booking.command.CancelBookingCommand;
import com.github.booking.application.booking.command.ConfirmBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingCommand;
import com.github.booking.application.booking.command.InitiateBookingResult;
import com.github.booking.application.booking.command.ReleaseSeatCommand;
import com.github.booking.application.booking.command.ReserveSeatCommand;
import com.github.booking.application.booking.query.BookingDetailView;
import com.github.booking.application.booking.query.BookingSummaryView;
import com.github.booking.application.booking.query.GetBookingQuery;
import com.github.booking.application.booking.query.ListBookingsQuery;
import com.github.booking.infrastructure.web.representation.GetBookingResponse;
import com.github.booking.infrastructure.web.representation.InitiateBookingResponse;
import com.github.booking.infrastructure.web.representation.ListBookingsResponse;
import com.github.seedwork.infrastructure.web.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingControllerTest extends ControllerTest {

  @MockitoBean
  private BookingCommandHandler bookingCommandHandler;
  @MockitoBean
  private BookingQueryHandler bookingQueryHandler;

  @Test
  void initiateBookingShouldReturnInitiateBookingResponse() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(InitiateBookingCommand.class);

    when(bookingCommandHandler.initiateBooking(commandCaptor.capture()))
      .thenReturn(new InitiateBookingResult("B00000000000000000"));

    // Act
    final var entity = client().post()
      .uri("/shows/{show_id}/bookings", "S00000000000000000")
      .retrieve()
      .toEntity(InitiateBookingResponse.class);

    // Assert
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("B00000000000000000", response.getBookingId());
    final var command = commandCaptor.getValue();

    assertEquals("S00000000000000000", command.showId());
  }

  @Test
  void reserveSeatShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ReserveSeatCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .reserveSeat(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/bookings/{booking_id}/reserved-seats/{seat_number}", "B00000000000000000", "A1")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
    assertEquals("A1", command.seatNumber());
  }

  @Test
  void releaseSeatShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ReleaseSeatCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .releaseSeat(commandCaptor.capture());

    // Act
    final var entity = client().delete()
      .uri("/bookings/{booking_id}/reserved-seats/{seat_number}", "B00000000000000000", "A1")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
    assertEquals("A1", command.seatNumber());
  }

  @Test
  void confirmBookingShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ConfirmBookingCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .confirmBooking(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/confirmed-bookings/{booking_id}", "B00000000000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
  }

  @Test
  void cancelBookingShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(CancelBookingCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .cancelBooking(commandCaptor.capture());

    // Act
    final var entity = client().delete()
      .uri("/bookings/{booking_id}", "B00000000000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
  }

  @Test
  void getBookingShouldReturnGetBookingResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(GetBookingQuery.class);

    when(bookingQueryHandler.getBooking(queryCaptor.capture()))
      .thenReturn(new BookingDetailView(
        "B00000000000000000",
        "INITIATED",
        "S00000000000000000",
        Instant.EPOCH));

    // Act
    final var entity = client().get()
      .uri("/bookings/{booking_id}", "B00000000000000000")
      .retrieve()
      .toEntity(GetBookingResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("B00000000000000000", response.getBookingId());
    assertEquals("INITIATED", response.getStatus());
    final var show = response.getShow();

    assertEquals("S00000000000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
    final var query = queryCaptor.getValue();

    assertEquals("B00000000000000000", query.bookingId());
  }

  @Test
  void listBookingsShouldReturnListBookingsResponse() {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListBookingsQuery.class);

    when(bookingQueryHandler.listBookings(queryCaptor.capture()))
      .thenReturn(List.of(new BookingSummaryView(
        "B00000000000000000",
        "INITIATED",
        "S00000000000000000",
        Instant.EPOCH)));

    // Act
    final var entity = client().get()
      .uri("/bookings")
      .retrieve()
      .toEntity(ListBookingsResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    final var bookings = response.getBookings();

    assertEquals(1, bookings.size());
    final var booking = bookings.getFirst();

    assertEquals("B00000000000000000", booking.getBookingId());
    assertEquals("INITIATED", booking.getStatus());
    final var show = booking.getShow();

    assertEquals("S00000000000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
    final var query = queryCaptor.getValue();

    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }
}
