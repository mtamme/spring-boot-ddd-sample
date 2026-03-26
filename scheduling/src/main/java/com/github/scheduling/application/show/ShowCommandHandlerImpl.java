package com.github.scheduling.application.show;

import com.github.scheduling.application.show.command.ScheduleShowCommand;
import com.github.scheduling.application.show.command.ScheduleShowResult;
import com.github.scheduling.domain.hall.HallId;
import com.github.scheduling.domain.hall.HallService;
import com.github.scheduling.domain.movie.MovieId;
import com.github.scheduling.domain.movie.MovieService;
import com.github.scheduling.domain.show.Show;
import com.github.scheduling.domain.show.ShowRepository;
import com.github.scheduling.domain.show.ShowSchedulingPolicy;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class ShowCommandHandlerImpl implements ShowCommandHandler {

  private final ShowRepository showRepository;
  private final MovieService movieService;
  private final HallService hallService;
  private final ShowSchedulingPolicy showSchedulingPolicy;

  public ShowCommandHandlerImpl(final ShowRepository showRepository,
                                final MovieService movieService,
                                final HallService hallService,
                                final ShowSchedulingPolicy showSchedulingPolicy) {
    this.showRepository = showRepository;
    this.movieService = movieService;
    this.hallService = hallService;
    this.showSchedulingPolicy = showSchedulingPolicy;
  }

  @Override
  public ScheduleShowResult scheduleShow(final ScheduleShowCommand command) {
    final var movie = movieService.movieFrom(new MovieId(command.movieId()));
    final var hall = hallService.hallFrom(new HallId(command.hallId()));
    final var end = command.scheduledAt().plus(movie.runtimeMinutes(), ChronoUnit.MINUTES);

    showSchedulingPolicy.ensureNoOverlap(hall.hallId(), command.scheduledAt(), end);

    final var show = new Show(
      showRepository.nextShowId(),
      command.scheduledAt(),
      movie,
      hall);

    showRepository.save(show);

    return new ScheduleShowResult(show.showId().value());
  }
}
