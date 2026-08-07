-- V2__add_upload_status_to_chapters.sql
-- Add upload_status column to chapters table to match Chapter JPA Entity

ALTER TABLE chapters ADD COLUMN IF NOT EXISTS upload_status VARCHAR(50) DEFAULT 'PENDING';
