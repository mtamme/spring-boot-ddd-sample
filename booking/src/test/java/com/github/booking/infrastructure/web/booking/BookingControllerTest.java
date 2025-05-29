package com.github.booking.infrastructure.web.booking;

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
import com.github.booking.infrastructure.web.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerTest extends ControllerTest {

  @MockitoBean
  private BookingCommandHandler bookingCommandHandler;
  @MockitoBean
  private BookingQueryHandler bookingQueryHandler;

  @Test
  void initiateBookingShouldReturnInitiateBookingResponse() throws Exception {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(InitiateBookingCommand.class);

    when(bookingCommandHandler.initiateBooking(commandCaptor.capture()))
      .thenReturn(new InitiateBookingResult("B00000000000000000"));

    // Act
    // Assert
    mockMvc().perform(post("/shows/{show_id}/bookings", "S00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.bookingId").value("B00000000000000000"));
    final var command = commandCaptor.getValue();

    assertEquals("S00000000000000000", command.showId());
  }

  @Test
  void reserveSeatShouldReturnNoContent() throws Exception {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ReserveSeatCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .reserveSeat(commandCaptor.capture());

    // Act
    // Assert
    mockMvc().perform(put("/bookings/{booking_id}/reserved-seats/{seat_number}", "B00000000000000000", "A1")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
    assertEquals("A1", command.seatNumber());
  }

  @Test
  void releaseSeatShouldReturnNoContent() throws Exception {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ReleaseSeatCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .releaseSeat(commandCaptor.capture());

    // Act
    // Assert
    mockMvc().perform(delete("/bookings/{booking_id}/reserved-seats/{seat_number}", "B00000000000000000", "A1")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
    assertEquals("A1", command.seatNumber());
  }

  @Test
  void confirmBookingShouldReturnNoContent() throws Exception {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(ConfirmBookingCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .confirmBooking(commandCaptor.capture());

    // Act
    // Assert
    mockMvc().perform(put("/confirmed-bookings/{booking_id}", "B00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
  }

  @Test
  void cancelBookingShouldReturnNoContent() throws Exception {
    // Arrange
    final var commandCaptor = ArgumentCaptor.forClass(CancelBookingCommand.class);

    doNothing()
      .when(bookingCommandHandler)
      .cancelBooking(commandCaptor.capture());

    // Act
    // Assert
    mockMvc().perform(delete("/bookings/{booking_id}", "B00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
    final var command = commandCaptor.getValue();

    assertEquals("B00000000000000000", command.bookingId());
  }

  @Test
  void getBookingShouldReturnGetBookingResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(GetBookingQuery.class);

    when(bookingQueryHandler.getBooking(queryCaptor.capture()))
      .thenReturn(new BookingDetailView(
        "B00000000000000000",
        "INITIATED",
        "S00000000000000000",
        Instant.EPOCH));

    // Act
    // Assert
    mockMvc().perform(get("/bookings/{booking_id}", "B00000000000000000")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.bookingId").value("B00000000000000000"))
      .andExpect(jsonPath("$.status").value("INITIATED"))
      .andExpect(jsonPath("$.show.showId").value("S00000000000000000"))
      .andExpect(jsonPath("$.show.scheduledAt").value("1970-01-01T00:00:00Z"));
    final var query = queryCaptor.getValue();

    assertEquals("B00000000000000000", query.bookingId());
  }

  @Test
  void listBookingsShouldReturnListBookingsResponse() throws Exception {
    // Arrange
    final var queryCaptor = ArgumentCaptor.forClass(ListBookingsQuery.class);

    when(bookingQueryHandler.listBookings(queryCaptor.capture()))
      .thenReturn(List.of(new BookingSummaryView(
        "B00000000000000000",
        "INITIATED",
        "S00000000000000000",
        Instant.EPOCH)));

    // Act
    // Assert
    mockMvc().perform(get("/bookings")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.bookings.length()").value(1))
      .andExpect(jsonPath("$.bookings[0].bookingId").value("B00000000000000000"))
      .andExpect(jsonPath("$.bookings[0].status").value("INITIATED"))
      .andExpect(jsonPath("$.bookings[0].show.showId").value("S00000000000000000"))
      .andExpect(jsonPath("$.bookings[0].show.scheduledAt").value("1970-01-01T00:00:00Z"));
    final var query = queryCaptor.getValue();

    assertEquals(0L, query.offset());
    assertEquals(10, query.limit());
  }
}
