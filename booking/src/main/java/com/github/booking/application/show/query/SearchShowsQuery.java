package com.github.booking.application.show.query;

public record SearchShowsQuery(String term, long offset, int limit) {
}
