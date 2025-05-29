package com.github.booking.application.show.query;

public record SearchShowsQuery(String query, long offset, int limit) {
}
