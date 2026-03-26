CREATE SEQUENCE show_s START WITH 1 INCREMENT BY 50;

CREATE TABLE show
(
    id_                   BIGINT      NOT NULL PRIMARY KEY,
    version_              BIGINT      NOT NULL,
    show_id               VARCHAR(18) NOT NULL,
    scheduled_at          TIMESTAMP   NOT NULL,
    movie_id              VARCHAR(18) NOT NULL,
    movie_title           TEXT        NOT NULL,
    movie_runtime_minutes INTEGER     NOT NULL,
    hall_id               VARCHAR(18) NOT NULL,
    hall_name             TEXT        NOT NULL,
    hall_seat_count       INTEGER     NOT NULL,

    CONSTRAINT uq_show_show_id UNIQUE (show_id)
);
