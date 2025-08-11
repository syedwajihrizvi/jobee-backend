ALTER TABLE jobs
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN employment_type VARCHAR(50),
    ADD COLUMN min_salary INT,
    ADD COLUMN max_salary INT;