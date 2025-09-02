CREATE TABLE interview_conductors (
    interviewer_id BIGINT NOT NULL,
    interview_id BIGINT NOT NULL,
    PRIMARY KEY (interviewer_id, interview_id),
    FOREIGN KEY (interviewer_id) REFERENCES business_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE
);