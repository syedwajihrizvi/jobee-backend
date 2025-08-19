CREATE TABLE IF NOT EXISTS user_favorite_jobs (
    user_profile_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    PRIMARY KEY (user_profile_id, job_id),
    FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE ON UPDATE CASCADE
);