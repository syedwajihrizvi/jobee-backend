ALTER TABLE interviews
    DROP COLUMN duration;
ALTER TABLE interviews
    ADD COLUMN end_time TIME NOT NULL;