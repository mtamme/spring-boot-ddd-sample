package com.github.booking.application.show;

import com.github.booking.application.show.command.ScheduleShowCommand;
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
public class ShowCommandHandlerImpl implements ShowCommandHandler {

  private final ShowRepository showRepository;
  private final MovieService movieService;
  private final HallService hallService;

  public ShowCommandHandlerImpl(final ShowRepository showRepository,
                                final MovieService movieService,
                                final HallService hallService) {
    this.showRepository = Objects.requireNonNull(showRepository);
    this.movieService = Objects.requireNonNull(movieService);
    this.hallService = Objects.requireNonNull(hallService);
  }

  @Override
  public void scheduleShow(final ScheduleShowCommand command) {
    final var show = new Show(
      new ShowId(command.showId()),
      command.scheduledAt(),
      movieService.movieFrom(new MovieId(command.movieId())),
      hallService.hallFrom(new HallId(command.hallId())));

    showRepository.save(show);
  }
}
