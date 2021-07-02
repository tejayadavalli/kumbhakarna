CREATE TABLE revenueInfo (
    id date NOT NULL PRIMARY KEY,
    courtOne jsonb  NOT NULL,
    courtTwo jsonb NOT NULL
  );
ALTER table revenueinfo ADD COLUMN expenses jsonb;