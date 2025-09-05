ALTER TABLE user_profiles
    ADD COLUMN primary_resume_id BIGINT,
    ADD FOREIGN KEY (primary_resume_id) REFERENCES user_documents(id);