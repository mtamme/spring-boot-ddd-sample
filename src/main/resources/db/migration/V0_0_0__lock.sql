CREATE TABLE int_lock
(
  lock_key     TEXT      NOT NULL,
  region       TEXT      NOT NULL,
  client_id    TEXT,
  created_date TIMESTAMP NOT NULL,
  CONSTRAINT int_lock_pk PRIMARY KEY (lock_key, region)
);
