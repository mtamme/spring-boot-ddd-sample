package com.github.scheduling.application.movie;

import com.github.scheduling.application.movie.query.ListMoviesQuery;
import com.github.scheduling.application.movie.query.MovieView;
import com.github.scheduling.domain.movie.MovieFixture;
import com.github.scheduling.domain.movie.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieQueryHandlerImplTest {

  @Mock
  private MovieService movieService;

  @Test
  void listMoviesShouldReturnAllMovies() {
    // Arrange
    when(movieService.listMovies())
      .thenReturn(List.of(
        MovieFixture.newMovie("M00000000000000001"),
        MovieFixture.newMovie("M00000000000000002")));
    final var handler = new MovieQueryHandlerImpl(movieService);

    // Act
    final var result = handler.listMovies(new ListMoviesQuery());

    // Assert
    assertEquals(2, result.size());
    assertEquals(new MovieView("M00000000000000001", "TestTitle", 120), result.get(0));
    assertEquals(new MovieView("M00000000000000002", "TestTitle", 120), result.get(1));
  }
}
