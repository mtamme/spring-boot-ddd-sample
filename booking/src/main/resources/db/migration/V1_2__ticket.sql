CREATE TABLE ticket
(
  id_                          BIGINT    NOT NULL,
  version_                     INTEGER   NOT NULL,
  booking_id                   TEXT      NOT NULL,
  ticket_id                    TEXT      NOT NULL,
  status                       TEXT      NOT NULL,
  seat_assignment_movie_title  TEXT      NOT NULL,
  seat_assignment_hall_name    TEXT      NOT NULL,
  seat_assignment_scheduled_at TIMESTAMP NOT NULL,
  seat_assignment_seat_number  TEXT      NOT NULL,
  CONSTRAINT ticket_pk PRIMARY KEY (id_)
);

CREATE SEQUENCE ticket_s
  START WITH 1
  INCREMENT BY 50;

CREATE UNIQUE INDEX ticket_ticket_id_ui
  ON ticket (ticket_id);
