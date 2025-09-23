ALTER TABLE interview_preparations
    DROP CONSTRAINT IF EXISTS interview_preparation_resouces_interview_preparation_id_fkey;

DROP TABLE IF EXISTS interview_preparation_resouces;

CREATE TABLE interview_preparation_resources (
    id BIGSERIAL PRIMARY KEY,
    interview_preparation_id BIGINT NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    resource_link VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_preparation_id) REFERENCES interview_preparations(id) ON DELETE CASCADE
);