CREATE TABLE revenueInfo (
    id date NOT NULL PRIMARY KEY,
    courtOne jsonb  NOT NULL,
    courtTwo jsonb NOT NULL
  );
ALTER table revenueinfo ADD COLUMN expenses jsonb;


CREATE TABLE revenueInfo (
  id1 text,
  id2 text
);


CREATE TABLE configuration_meta (
  config_id varchar(30) PRIMARY KEY,
  description text,
  created_at TIMESTAMP,
  access_groups varchar(10)[],
  updated_at TIMESTAMP,
  latest_version int
);


CREATE TYPE key_type AS ENUM('BOOLEAN', 'STRING', 'NUMBER', 'JSON');

CREATE TABLE key_meta (
  config_id varchar(30),
  key_id varchar(30),
  name varchar(20),
  key_type key_type,
  default_value text,
  latest_version int,
  created_at TIMESTAMP,
  access_group varchar(10)[],
  PRIMARY KEY (config_id, key_id),
CONSTRAINT fk_config_id
FOREIGN KEY(config_id)
REFERENCES configuration_meta(config_id),
UNIQUE(key_id)
);

CREATE TABLE key_coditions (
  key_id varchar(30),
  version int,
  expression text,
  priority int,
  value text,
  description text,
  created_at TIMESTAMP,
CONSTRAINT fk_key_id
FOREIGN KEY(key_id)
REFERENCES key_meta(key_id)
);

CREATE TABLE config_key_versions_mappings (
  config_id varchar(30),
  config_version int,
  key_id varchar(30),
  key_version int,
  CONSTRAINT fk_config_id
  FOREIGN KEY(config_id)
  REFERENCES configuration_meta(config_id),
  CONSTRAINT fk_key_id
  FOREIGN KEY(key_id)
  REFERENCES key_meta(key_id)
);


CREATE TABLE config_evaluations (
  config_id varchar(30),
  version int,
  md5Hash varchar(16),
  key text,
  value text,
  key_type key_type,
  crated_at TIMESTAMP,
CONSTRAINT fk_config_id
FOREIGN KEY(config_id)
REFERENCES configuration_meta(config_id)
);