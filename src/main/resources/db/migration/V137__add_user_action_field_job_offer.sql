ALTER TABLE unoffical_job_offers
ADD COLUMN user_action BOOLEAN DEFAULT FALSE;

ALTER TABLE unoffical_job_offers RENAME TO unofficial_job_offers;