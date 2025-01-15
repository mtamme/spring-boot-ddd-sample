CREATE TABLE outbox_message
(
  message_id     TEXT      NOT NULL,
  type           TEXT      NOT NULL,
  correlation_id TEXT      NOT NULL,
  enqueued_at    TIMESTAMP NOT NULL,
  scheduled_at   TIMESTAMP NOT NULL,
  requeue_count  INTEGER   NOT NULL,
  payload        BYTEA     NOT NULL,
  CONSTRAINT outbox_message_pk PRIMARY KEY (message_id)
);

CREATE INDEX outbox_message_scheduled_at_i
  ON outbox_message (scheduled_at);
