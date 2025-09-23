ALTER TABLE interview_preparation_resources
    ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE interview_preparation_resources
    RENAME COLUMN resource_name to title;

ALTER TABLE interview_preparation_resources
    RENAME COLUMN resource_link to link;

ALTER TABLE interview_preparation_resources
    ADD COLUMN IF NOT EXISTS type VARCHAR(100);

