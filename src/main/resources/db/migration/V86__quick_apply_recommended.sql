CREATE TABLE quick_apply_recommended_jobs (
    id BIGSERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    quick_apply_count INT NOT NULL DEFAULT 0,
    last_quick_apply TIMESTAMPTZ NOT NULL DEFAULT (NOW() - INTERVAL '6 hours'),
    FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE
);