CREATE TABLE show
(
  id_                           BIGINT    NOT NULL,
  version_                      BIGINT    NOT NULL,
  show_id                       TEXT      NOT NULL,
  scheduled_at                  TIMESTAMP NOT NULL,
  hall_id                       TEXT      NOT NULL,
  hall_name                     TEXT      NOT NULL,
  hall_seat_layout_row_count    INTEGER   NOT NULL,
  hall_seat_layout_column_count INTEGER   NOT NULL,
  movie_id                      TEXT      NOT NULL,
  movie_title                   TEXT      NOT NULL,
  CONSTRAINT show_pk PRIMARY KEY (id_)
);

CREATE SEQUENCE show_s
  START WITH 1
  INCREMENT BY 50;

CREATE UNIQUE INDEX show_show_id_ui
  ON show (show_id);

CREATE TABLE show_seat
(
  index_      INTEGER NOT NULL,
  show_id_    BIGINT  NOT NULL,
  seat_number TEXT    NOT NULL,
  status      TEXT    NOT NULL,
  booking_id  TEXT,
  CONSTRAINT show_seat_pk PRIMARY KEY (index_, show_id_),
  CONSTRAINT show_seat_show_fk FOREIGN KEY (show_id_) REFERENCES show
);

CREATE UNIQUE INDEX show_seat_show_id_seat_number_ui
  ON show_seat (show_id_, seat_number);
