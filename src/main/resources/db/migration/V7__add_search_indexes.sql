-- V7__add_search_indexes.sql
-- Add indexes to improve search, filter, and sorting performance on comics table

CREATE INDEX IF NOT EXISTS idx_comics_title ON comics(title);
CREATE INDEX IF NOT EXISTS idx_comics_author ON comics(author);
CREATE INDEX IF NOT EXISTS idx_comics_status ON comics(status);
CREATE INDEX IF NOT EXISTS idx_comics_created_at ON comics(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comics_view_count ON comics(view_count DESC);
CREATE INDEX IF NOT EXISTS idx_comics_rating ON comics(rating DESC);
