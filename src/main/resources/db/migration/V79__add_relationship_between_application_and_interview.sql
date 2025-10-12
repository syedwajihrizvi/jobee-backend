ALTER TABLE interviews
ADD COLUMN application_id BIGINT,
ADD CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES applications(id);