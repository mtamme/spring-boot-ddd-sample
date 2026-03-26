package com.github.scheduling.domain.hall;

import java.util.List;

public interface HallService {

  Hall hallFrom(HallId hallId);

  List<Hall> listHalls();
}
