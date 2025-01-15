package com.github.booking.api;

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
import com.github.booking.application.booking.view.BookingDetailView;
import com.github.booking.application.booking.view.BookingSummaryView;
import com.github.seedwork.api.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingControllerTest extends ControllerTest {

  @MockitoBean
  private BookingCommandHandler bookingCommandHandler;
  @MockitoBean
  private BookingQueryHandler bookingQueryHandler;

  @Test
  void initiateBookingShouldReturnInitiateBookingResponse() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(InitiateBookingCommand.class);

    Mockito.when(bookingCommandHandler.initiateBooking(commandCaptor.capture()))
      .thenReturn("10000000000");

    // Act
    final var entity = client().post()
      .uri("/shows/{show_id}/bookings", "40000000000")
      .retrieve()
      .toEntity(InitiateBookingResponse.class);

    // Assert
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("10000000000", response.getBookingId());
    final var command = commandCaptor.getValue();

    assertEquals("40000000000", command.showId());
  }

  @Test
  void reserveSeatShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ReserveSeatCommand.class);

    Mockito.doNothing()
      .when(bookingCommandHandler)
      .reserveSeat(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/bookings/{booking_id}/reserved-seats/{seat_number}", "10000000000", "A1")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("10000000000", command.bookingId());
    assertEquals("A1", command.seatNumber());
  }

  @Test
  void releaseSeatShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ReleaseSeatCommand.class);

    Mockito.doNothing()
      .when(bookingCommandHandler)
      .releaseSeat(commandCaptor.capture());

    // Act
    final var entity = client().delete()
      .uri("/bookings/{booking_id}/reserved-seats/{seat_number}", "10000000000", "A1")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("10000000000", command.bookingId());
    assertEquals("A1", command.seatNumber());
  }

  @Test
  void confirmBookingShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ConfirmBookingCommand.class);

    Mockito.doNothing()
      .when(bookingCommandHandler)
      .confirmBooking(commandCaptor.capture());

    // Act
    final var entity = client().put()
      .uri("/confirmed-bookings/{booking_id}", "10000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("10000000000", command.bookingId());
  }

  @Test
  void cancelBookingShouldReturnNoContent() {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(CancelBookingCommand.class);

    Mockito.doNothing()
      .when(bookingCommandHandler)
      .cancelBooking(commandCaptor.capture());

    // Act
    final var entity = client().delete()
      .uri("/bookings/{booking_id}", "10000000000")
      .retrieve()
      .toBodilessEntity();

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    final var command = commandCaptor.getValue();

    assertEquals("10000000000", command.bookingId());
  }

  @Test
  void getBookingShouldReturnGetBookingResponse() {
    // Arrange
    Mockito.when(bookingQueryHandler.getBooking("10000000000"))
      .thenReturn(new BookingDetailView(
        "10000000000",
        "INITIATED",
        "40000000000",
        Instant.EPOCH));

    // Act
    final var entity = client().get()
      .uri("/bookings/{booking_id}", "10000000000")
      .retrieve()
      .toEntity(GetBookingResponse.class);

    // Assert
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    final var response = entity.getBody();

    assertNotNull(response);
    assertEquals("10000000000", response.getBookingId());
    assertEquals("INITIATED", response.getStatus());
    final var show = response.getShow();

    assertEquals("40000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
  }

  @Test
  void listBookingsShouldReturnListBookingsResponse() {
    // Arrange
    Mockito.when(bookingQueryHandler.listBookings(0, 10))
      .thenReturn(List.of(new BookingSummaryView(
        "10000000000",
        "INITIATED",
        "40000000000",
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

    assertEquals("10000000000", booking.getBookingId());
    assertEquals("INITIATED", booking.getStatus());
    final var show = booking.getShow();

    assertEquals("40000000000", show.getShowId());
    assertEquals(Instant.EPOCH, show.getScheduledAt());
  }
}
