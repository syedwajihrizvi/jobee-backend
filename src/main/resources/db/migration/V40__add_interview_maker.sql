ALTER TABLE interviews
    ADD COLUMN created_by_user_id BIGINT,
    ADD FOREIGN KEY (created_by_user_id) REFERENCES business_accounts(id);