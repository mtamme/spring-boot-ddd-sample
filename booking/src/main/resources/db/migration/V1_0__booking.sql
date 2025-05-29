CREATE TABLE booking
(
  id_        BIGINT NOT NULL,
  version_   BIGINT NOT NULL,
  show_id    TEXT   NOT NULL,
  booking_id TEXT   NOT NULL,
  status     TEXT   NOT NULL,
  CONSTRAINT booking_pk PRIMARY KEY (id_)
);

CREATE SEQUENCE booking_s
  START WITH 1
  INCREMENT BY 50;

CREATE UNIQUE INDEX booking_booking_id_ui
  ON booking (booking_id);
