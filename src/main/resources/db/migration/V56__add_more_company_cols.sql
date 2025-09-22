ALTER TABLE companies
    ADD COLUMN website VARCHAR(255),
    ADD COLUMN founded_year INT,
    ADD COLUMN num_employees INT,
    ADD COLUMN industry VARCHAR(100),
    ADD COLUMN description TEXT;