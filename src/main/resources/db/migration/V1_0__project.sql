CREATE TABLE project
(
  id          BIGINT  NOT NULL,
  version     INTEGER NOT NULL,
  project_id  TEXT    NOT NULL,
  archived    BOOLEAN NOT NULL,
  name        TEXT    NOT NULL,
  description TEXT    NOT NULL,
  CONSTRAINT project_pk PRIMARY KEY (id)
);

CREATE SEQUENCE project_s
  START WITH 1
  INCREMENT BY 50;

CREATE UNIQUE INDEX project_project_id_ui
  ON project (project_id);
