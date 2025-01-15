CREATE TABLE task
(
  id            BIGINT    NOT NULL,
  version       INTEGER   NOT NULL,
  project_id    TEXT      NOT NULL,
  task_id       TEXT      NOT NULL,
  status        TEXT      NOT NULL,
  opened_at     TIMESTAMP NOT NULL,
  started_at    TIMESTAMP,
  closed_at     TIMESTAMP,
  summary       TEXT      NOT NULL,
  description   TEXT      NOT NULL,
  assignee_name TEXT,
  CONSTRAINT task_pk PRIMARY KEY (id)
);

CREATE SEQUENCE task_s
  START WITH 1
  INCREMENT BY 50;

CREATE UNIQUE INDEX task_task_id_ui
  ON task (task_id);
