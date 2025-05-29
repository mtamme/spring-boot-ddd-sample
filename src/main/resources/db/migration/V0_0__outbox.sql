CREATE TABLE outbox_message
(
  sequence_number BIGINT    NOT NULL,
  group_id        TEXT      NOT NULL,
  enqueued_at     TIMESTAMP NOT NULL,
  available_at    TIMESTAMP NOT NULL,
  lock_id         UUID,
  attempt_count   INTEGER   NOT NULL,
  subject         TEXT      NOT NULL,
  body            BYTEA     NOT NULL,
  CONSTRAINT outbox_message_pk PRIMARY KEY (sequence_number)
);

CREATE SEQUENCE outbox_message_s
  START WITH 1
  INCREMENT BY 50;

CREATE INDEX outbox_message_group_id_sequence_number_i
  ON outbox_message (group_id, sequence_number);

CREATE INDEX outbox_message_group_id_available_at_i
  ON outbox_message (group_id, available_at);

CREATE INDEX outbox_message_available_at_i
  ON outbox_message (available_at);

CREATE INDEX outbox_message_lock_id_available_at_i
  ON outbox_message (lock_id, available_at);
