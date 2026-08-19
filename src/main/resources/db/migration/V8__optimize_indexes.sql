-- V8__optimize_indexes.sql
-- Add indexes for analytics, user search, library and reports performance

CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_role_banned ON users(role, is_banned);

CREATE INDEX IF NOT EXISTS idx_reports_status_created ON reports(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_type_status ON reports(report_type, status);

CREATE INDEX IF NOT EXISTS idx_comics_user_id ON comics(user_id);
