ALTER TABLE user_projects
DROP COLUMN IF EXISTS year_completed;

ALTER TABLE user_projects
ADD COLUMN year_completed VARCHAR(12);