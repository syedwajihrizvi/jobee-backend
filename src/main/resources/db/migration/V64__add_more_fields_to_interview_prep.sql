CREATE TABLE interview_preparation_strengths (
    id BIGSERIAL PRIMARY KEY,
    interview_preparation_id BIGINT NOT NULL,
    strength TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_preparation_id) REFERENCES interview_preparations(id) ON DELETE CASCADE
);

CREATE TABLE interview_preparation_weaknesses (
    id BIGSERIAL PRIMARY KEY,
    interview_preparation_id BIGINT NOT NULL,
    weakness TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_preparation_id) REFERENCES interview_preparations(id) ON DELETE CASCADE
);

CREATE TABLE interview_preparation_questions (
    id BIGSERIAL PRIMARY KEY,
    interview_preparation_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_preparation_id) REFERENCES interview_preparations(id) ON DELETE CASCADE
);

CREATE TABLE interview_preparation_resouces (
    id BIGSERIAL PRIMARY KEY,
    interview_preparation_id BIGINT NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    resource_link VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_preparation_id) REFERENCES interview_preparations(id) ON DELETE CASCADE
);

ALTER TABLE interview_preparations
    ADD COLUMN overAllAdvice TEXT;