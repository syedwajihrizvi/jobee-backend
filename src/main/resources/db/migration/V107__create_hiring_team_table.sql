CREATE TABLE hiring_team (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    business_account_id BIGINT,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    invited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_hiring_team_job
        FOREIGN KEY (job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_hiring_team_business_account
        FOREIGN KEY (business_account_id)
        REFERENCES business_accounts(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
