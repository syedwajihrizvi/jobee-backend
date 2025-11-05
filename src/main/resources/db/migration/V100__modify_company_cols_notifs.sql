ALTER TABLE notifications
DROP COLUMN IF EXISTS company_id,
DROP COLUMN IF EXISTS company_name,
DROP COLUMN IF EXISTS company_logo_url,
DROP COLUMN IF EXISTS application_id,
DROP COLUMN IF EXISTS job_id;

-- Add nullable foreign key columns
ALTER TABLE notifications
ADD COLUMN company_id BIGINT,
ADD COLUMN application_id BIGINT,
ADD COLUMN job_id BIGINT;

-- Add the actual foreign key constraints
ALTER TABLE notifications
ADD CONSTRAINT fk_notifications_company
FOREIGN KEY (company_id) REFERENCES companies(id)
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE notifications
ADD CONSTRAINT fk_notifications_application
FOREIGN KEY (application_id) REFERENCES applications(id)
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE notifications
ADD CONSTRAINT fk_notifications_job
FOREIGN KEY (job_id) REFERENCES jobs(id)
ON DELETE SET NULL ON UPDATE CASCADE;
