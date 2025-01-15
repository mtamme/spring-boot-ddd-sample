package com.github.booking.infrastructure.persistence.show;

import com.github.booking.application.show.ShowQueryHandler;
import com.github.booking.application.show.view.ShowDetailView;
import com.github.booking.application.show.view.ShowSummaryView;
import com.github.booking.domain.show.ShowNotFoundException;
import com.github.seedwork.infrastructure.persistence.Search;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JpaShowQueryHandler implements ShowQueryHandler {

  private final JpaShowRepository repository;

  public JpaShowQueryHandler(final JpaShowRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public ShowDetailView getShow(final String showId) {
    final var show = repository.find(showId)
      .orElseThrow(ShowNotFoundException::new);
    final var seats = repository.findAllSeats(showId);

    show.seats().addAll(seats);

    return show;
  }

  @Override
  public List<ShowSummaryView> listShows(final long offset, final int limit) {
    return repository.findAll(offset, limit);
  }

  @Override
  public List<ShowSummaryView> searchShows(final String query, final long offset, final int limit) {
    final var search = Search.of(query);

    return repository.findAllByPattern(search.containsPattern(), search.startsWithPattern(), offset, limit);
  }
}
