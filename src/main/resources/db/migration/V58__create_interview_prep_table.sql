CREATE TABLE interview_preparations (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE
);

ALTER TABLE interviews
    ADD COLUMN preparation_id BIGINT,
    ADD FOREIGN KEY (preparation_id) REFERENCES interview_preparations(id) ON DELETE SET NULL;