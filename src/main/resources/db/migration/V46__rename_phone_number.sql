ALTER TABLE interviews
    DROP COLUMN IF EXISTS phoneNumber;
ALTER TABLE interviews
    ADD COLUMN phone_number VARCHAR(15);