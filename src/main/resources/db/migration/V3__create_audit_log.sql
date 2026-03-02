-- Flyway migration: create audit_log table
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGSERIAL PRIMARY KEY,
  action VARCHAR(50) NOT NULL,
  actor VARCHAR(80) NOT NULL,
  message VARCHAR(200),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_audit_createdat ON audit_log(created_at);

