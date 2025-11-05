ALTER TABLE notifications
ADD COLUMN company_id BIGINT,
ADD COLUMN company_name VARCHAR(150),
ADD COLUMN company_logo_url VARCHAR(255),
ADD COLUMN application_id BIGINT,
ADD COLUMN job_id BIGINT;