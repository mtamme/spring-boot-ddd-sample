package com.github.scheduling.infrastructure.web.show;

import com.github.scheduling.application.show.ShowQueryHandler;
import com.github.scheduling.application.show.query.GetShowQuery;
import com.github.scheduling.application.show.query.ListShowsQuery;
import com.github.scheduling.infrastructure.web.ShowOperations;
import com.github.scheduling.infrastructure.web.representation.ListShowsResponse;
import com.github.scheduling.infrastructure.web.representation.ShowDetailResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ShowController implements ShowOperations {

  private final ShowQueryHandler showQueryHandler;
  private final ShowMapper showMapper;

  public ShowController(final ShowQueryHandler showQueryHandler, final ShowMapper showMapper) {
    this.showQueryHandler = Objects.requireNonNull(showQueryHandler);
    this.showMapper = Objects.requireNonNull(showMapper);
  }

  @Override
  public ResponseEntity<ShowDetailResponse> getShow(final String showId) {
    final var show = showQueryHandler.getShow(new GetShowQuery(showId));
    final var body = showMapper.toShowDetailResponse(show);
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<ListShowsResponse> listShows(final Long offset, final Integer limit) {
    final var shows = showQueryHandler.listShows(new ListShowsQuery(offset, limit));
    final var body = showMapper.toListShowsResponse(shows);
    return ResponseEntity.ok(body);
  }
}
