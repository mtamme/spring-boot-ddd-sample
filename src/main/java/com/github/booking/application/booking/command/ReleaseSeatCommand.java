package com.github.booking.application.booking.command;

public record ReleaseSeatCommand(String bookingId, String seatNumber) {
}
