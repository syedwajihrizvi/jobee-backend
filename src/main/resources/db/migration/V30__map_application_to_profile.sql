ALTER TABLE applications
    DROP CONSTRAINT applications_user_account_id_fkey,
    ADD COLUMN IF NOT EXISTS user_profile_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_user_profile FOREIGN KEY (user_profile_id) REFERENCES user_profiles (id);