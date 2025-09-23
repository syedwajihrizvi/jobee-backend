CREATE TABLE interview_tips (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    tip TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE
);

ALTER TABLE interviews
    DROP COLUMN preparation_tips_from_interviewer;