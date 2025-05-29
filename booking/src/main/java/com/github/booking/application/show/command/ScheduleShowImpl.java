package com.github.booking.application.show.command;

import com.github.booking.domain.hall.HallId;
import com.github.booking.domain.hall.HallService;
import com.github.booking.domain.movie.MovieId;
import com.github.booking.domain.movie.MovieService;
import com.github.booking.domain.show.Show;
import com.github.booking.domain.show.ShowId;
import com.github.booking.domain.show.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class ScheduleShowImpl implements ScheduleShow {

  private final ShowRepository showRepository;
  private final MovieService movieService;
  private final HallService hallService;

  public ScheduleShowImpl(final ShowRepository showRepository,
                          final MovieService movieService,
                          final HallService hallService) {
    this.showRepository = Objects.requireNonNull(showRepository);
    this.movieService = Objects.requireNonNull(movieService);
    this.hallService = Objects.requireNonNull(hallService);
  }

  @Override
  public void handle(final Command command) {
    final var show = new Show(
      new ShowId(command.showId()),
      command.scheduledAt(),
      movieService.getMovie(new MovieId(command.movieId())),
      hallService.getHall(new HallId(command.hallId())));

    showRepository.save(show);
  }
}
