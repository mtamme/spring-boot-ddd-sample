package com.github.booking.application.booking.command;

public record ReserveSeatCommand(String bookingId, String seatNumber) {
}
