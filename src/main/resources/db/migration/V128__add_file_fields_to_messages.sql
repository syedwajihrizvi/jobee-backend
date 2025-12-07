-- Add file support fields to messages table
ALTER TABLE messages
ADD COLUMN file_url VARCHAR(500),
ADD COLUMN file_name VARCHAR(255),
ADD COLUMN file_type VARCHAR(50),
ADD COLUMN file_size BIGINT;

-- Make text nullable since messages can now be file-only
ALTER TABLE messages
ALTER COLUMN text DROP NOT NULL;
