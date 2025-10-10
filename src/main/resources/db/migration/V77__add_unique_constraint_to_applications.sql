ALTER TABLE applications
ADD CONSTRAINT unique_user_job_application
UNIQUE (user_profile_id, job_id);