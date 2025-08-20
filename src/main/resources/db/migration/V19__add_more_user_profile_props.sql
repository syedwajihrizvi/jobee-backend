ALTER TABLE user_profiles
ADD COLUMN company VARCHAR(255);

ALTER TABLE user_profiles
ADD COLUMN phone_number VARCHAR(20);

ALTER TABLE user_profiles
ADD COLUMN city VARCHAR(100);

ALTER TABLE user_profiles
ADD COLUMN country VARCHAR(100);
