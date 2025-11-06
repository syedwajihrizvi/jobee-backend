ALTER TABLE jobs
ADD COLUMN IF NOT EXISTS content_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE jobs
DROP COLUMN IF EXISTS updated_at;

DROP TRIGGER IF EXISTS update_jobs_updated_at ON jobs;
DROP FUNCTION IF EXISTS update_jobs_updated_at_column();
DROP TRIGGER IF EXISTS update_jobs_content_updated_at ON jobs;
DROP FUNCTION IF EXISTS update_jobs_content_updated_at_column();

CREATE OR REPLACE FUNCTION update_jobs_content_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    IF (
        NEW.title IS DISTINCT FROM OLD.title OR
        NEW.description IS DISTINCT FROM OLD.description OR
        NEW.employment_type IS DISTINCT FROM OLD.employment_type OR
        NEW.location IS DISTINCT FROM OLD.location OR
        NEW.min_salary IS DISTINCT FROM OLD.min_salary OR
        NEW.max_salary IS DISTINCT FROM OLD.max_salary OR
        NEW.setting IS DISTINCT FROM OLD.setting OR
        NEW.department IS DISTINCT FROM OLD.department OR
        NEW.city IS DISTINCT FROM OLD.city OR
        NEW.state IS DISTINCT FROM OLD.state OR
        NEW.country IS DISTINCT FROM OLD.country OR
        NEW.level IS DISTINCT FROM OLD.level
    ) THEN
        NEW.content_updated_at = NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_jobs_content_updated_at
BEFORE UPDATE ON jobs
FOR EACH ROW
EXECUTE FUNCTION update_jobs_content_updated_at_column();