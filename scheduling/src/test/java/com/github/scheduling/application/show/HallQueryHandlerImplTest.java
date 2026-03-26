package com.github.scheduling.application.show;

import com.github.scheduling.application.show.query.HallView;
import com.github.scheduling.application.show.query.ListHallsQuery;
import com.github.scheduling.domain.hall.HallFixture;
import com.github.scheduling.domain.hall.HallService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallQueryHandlerImplTest {

  @Mock
  private HallService hallService;

  @Test
  void listHallsShouldReturnAllHalls() {
    // Arrange
    when(hallService.listHalls())
      .thenReturn(List.of(
        HallFixture.newHall("H00000000000000001"),
        HallFixture.newHall("H00000000000000002")));
    final var handler = new HallQueryHandlerImpl(hallService);

    // Act
    final var result = handler.listHalls(new ListHallsQuery());

    // Assert
    assertEquals(2, result.size());
    assertEquals(new HallView("H00000000000000001", "TestName", 150), result.get(0));
    assertEquals(new HallView("H00000000000000002", "TestName", 150), result.get(1));
  }
}
