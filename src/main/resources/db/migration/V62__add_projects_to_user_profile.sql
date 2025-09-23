CREATE TABLE user_projects (
    id BIGSERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    project_description TEXT,
    project_link VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE
);